import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { bankingService, Transaction as BankingTransaction, Account } from '../services/bankingService';
import { extractErrorMessage, isClientAccessError } from '../utils/errorHandler';

interface Transaction {
  id: number;
  transactionType: string;
  amount: number;
  balanceAfter: number;
  description: string;
  referenceNumber: string;
  fromAccount?: string;
  toAccount?: string;
  createdAt: string;
}

const Transactions: React.FC = () => {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [filteredTransactions, setFilteredTransactions] = useState<Transaction[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({
    type: '',
    dateRange: '30'
  });
  const [searchTerm, setSearchTerm] = useState('');
  
  // Transaction form state
  const [showTransactionForm, setShowTransactionForm] = useState(false);
  const [transactionType, setTransactionType] = useState<'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER'>('DEPOSIT');
  const [selectedAccount, setSelectedAccount] = useState<number | ''>('');
  const [destinationAccount, setDestinationAccount] = useState<number | ''>('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [processingTransaction, setProcessingTransaction] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        if (user?.id) {
          const [accountsData, transactionsData] = await Promise.all([
            bankingService.getAccountsByClient(user.id),
            bankingService.getTransactionsByClient(user.id)
          ]);
          
          setAccounts(accountsData);
          setTransactions(transactionsData);
          setFilteredTransactions(transactionsData);
        }
      } catch (error: any) {
        console.error('Error fetching data:', error);
        setError(extractErrorMessage(error));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [user?.id]);

  // Check if we should show transaction form based on URL params
  useEffect(() => {
    const action = searchParams.get('action');
    if (action === 'deposit' || action === 'withdraw') {
      setTransactionType(action === 'deposit' ? 'DEPOSIT' : 'WITHDRAWAL');
      setShowTransactionForm(true);
    }
  }, [searchParams]);

  useEffect(() => {
    let filtered = transactions;

    // Filter by type
    if (filters.type) {
      filtered = filtered.filter(t => t.transactionType === filters.type);
    }



    // Filter by date range
    if (filters.dateRange) {
      const days = parseInt(filters.dateRange);
      const cutoffDate = new Date();
      cutoffDate.setDate(cutoffDate.getDate() - days);
      filtered = filtered.filter(t => new Date(t.createdAt) >= cutoffDate);
    }

    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(t => 
        t.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.referenceNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.transactionType.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    setFilteredTransactions(filtered);
  }, [transactions, filters, searchTerm]);

  const handleFilterChange = (key: string, value: string) => {
    setFilters(prev => ({
      ...prev,
      [key]: value
    }));
  };

  const clearFilters = () => {
    setFilters({
      type: '',
      dateRange: '30'
    });
    setSearchTerm('');
  };

  const handleTransactionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!selectedAccount || !amount) return;
    
    if (transactionType === 'TRANSFER' && !destinationAccount) {
      setError('Please select a destination account for transfer');
      return;
    }
    
    setProcessingTransaction(true);
    try {
      const transactionRequest = {
        accountId: selectedAccount as number,
        transactionType: transactionType,
        amount: parseFloat(amount),
        description: description || `${transactionType} transaction`,
        destinationAccountId: transactionType === 'TRANSFER' ? destinationAccount as number : undefined
      };
      
      let newTransaction: Transaction;
      if (transactionType === 'DEPOSIT') {
        newTransaction = await bankingService.processDeposit(transactionRequest);
      } else if (transactionType === 'WITHDRAWAL') {
        newTransaction = await bankingService.processWithdrawal(transactionRequest);
      } else if (transactionType === 'TRANSFER') {
        newTransaction = await bankingService.processTransfer(transactionRequest);
      } else {
        throw new Error(`Unsupported transaction type: ${transactionType}`);
      }
      
      // Add new transaction to the list
      setTransactions(prev => [newTransaction, ...prev]);
      setFilteredTransactions(prev => [newTransaction, ...prev]);
      
      // Update account balances
      setAccounts(prev => prev.map(acc => {
        if (acc.id === selectedAccount) {
          return { ...acc, balance: newTransaction.balanceAfter };
        }
        if (transactionType === 'TRANSFER' && acc.id === destinationAccount) {
          // For transfers, we need to update both accounts
          return { ...acc, balance: acc.balance + parseFloat(amount) };
        }
        return acc;
      }));
      
      // Reset form and close modal
      setSelectedAccount('');
      setDestinationAccount('');
      setAmount('');
      setDescription('');
      setShowTransactionForm(false);
      setError('');
      
      // Show success message
      alert(`${transactionType} successful! New balance: $${newTransaction.balanceAfter.toFixed(2)}`);
      
    } catch (error: any) {
      setError(extractErrorMessage(error));
    } finally {
      setProcessingTransaction(false);
    }
  };

  const getTransactionIcon = (type: string) => {
    switch (type) {
      case 'DEPOSIT':
        return (
          <div className="p-2 bg-green-100 rounded-lg">
            <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
          </div>
        );
      case 'WITHDRAWAL':
        return (
          <div className="p-2 bg-red-100 rounded-lg">
            <svg className="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
            </svg>
          </div>
        );
      case 'TRANSFER':
        return (
          <div className="p-2 bg-blue-100 rounded-lg">
            <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
            </svg>
          </div>
        );
      case 'PAYMENT':
        return (
          <div className="p-2 bg-purple-100 rounded-lg">
            <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
          </div>
        );
      default:
        return (
          <div className="p-2 bg-gray-100 rounded-lg">
            <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
        );
    }
  };



  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading transactions...</p>
        </div>
      </div>
    );
  }

  if (error) {
    const isAccessError = isClientAccessError({ response: { data: { errorCode: error } } });
    
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className={`text-6xl mb-4 ${isAccessError ? 'text-orange-500' : 'text-red-600'}`}>
            {isAccessError ? '🚫' : '⚠️'}
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            {isAccessError ? 'Access Restricted' : 'Error Loading Transactions'}
          </h2>
          <p className="text-gray-600 mb-4">{error}</p>
          {isAccessError && (
            <div className="bg-orange-50 border border-orange-200 rounded-lg p-4 mb-4 max-w-md mx-auto">
              <p className="text-orange-800 text-sm">
                <strong>Note:</strong> This appears to be an account access issue. 
                Please contact customer support if you believe this is an error.
              </p>
            </div>
          )}
          <button 
            onClick={() => window.location.reload()}
            className="btn-primary"
          >
            Try again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Transactions</h1>
            <p className="text-gray-600">View and manage your banking transactions.</p>
          </div>
                     <div className="flex space-x-3">
             <button
               onClick={() => {
                 setTransactionType('DEPOSIT');
                 setShowTransactionForm(true);
               }}
               className="btn-primary"
             >
               <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                 <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
               </svg>
               New Deposit
             </button>
             <button
               onClick={() => {
                 setTransactionType('WITHDRAWAL');
                 setShowTransactionForm(true);
               }}
               className="btn-secondary"
             >
               <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                 <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
               </svg>
               New Withdrawal
             </button>
             <button
               onClick={() => {
                 setTransactionType('TRANSFER');
                 setShowTransactionForm(true);
               }}
               className="btn-secondary"
             >
               <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                 <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
               </svg>
               New Transfer
             </button>
           </div>
        </div>

        {/* Filters and Search */}
                 <div className="bg-white rounded-lg shadow p-6 mb-8">
           <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Transaction Type</label>
              <select
                value={filters.type}
                onChange={(e) => handleFilterChange('type', e.target.value)}
                className="input-field"
              >
                <option value="">All Types</option>
                <option value="DEPOSIT">Deposit</option>
                <option value="WITHDRAWAL">Withdrawal</option>
                <option value="TRANSFER">Transfer</option>
                <option value="PAYMENT">Payment</option>
                <option value="REFUND">Refund</option>
              </select>
            </div>



            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Date Range</label>
              <select
                value={filters.dateRange}
                onChange={(e) => handleFilterChange('dateRange', e.target.value)}
                className="input-field"
              >
                <option value="7">Last 7 days</option>
                <option value="30">Last 30 days</option>
                <option value="90">Last 90 days</option>
                <option value="365">Last year</option>
                <option value="">All time</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Search</label>
              <input
                type="text"
                placeholder="Search transactions..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input-field"
              />
            </div>
          </div>

          <div className="flex justify-between items-center">
            <p className="text-sm text-gray-600">
              Showing {filteredTransactions.length} of {transactions.length} transactions
            </p>
            <button
              onClick={clearFilters}
              className="btn-secondary"
            >
              Clear Filters
            </button>
          </div>
        </div>

        {/* Transactions List */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-medium text-gray-900">Transaction History</h2>
          </div>
          
          <div className="p-6">
            {filteredTransactions.length === 0 ? (
              <div className="text-center py-12">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-900">No transactions found</h3>
                <p className="mt-1 text-sm text-gray-500">
                  Try adjusting your filters or search terms.
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {filteredTransactions.map((transaction) => (
                  <div key={transaction.id} className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition-colors">
                    <div className="flex items-start justify-between">
                      <div className="flex items-start space-x-4">
                        {getTransactionIcon(transaction.transactionType)}
                        
                        <div className="flex-1">
                                                     <div className="flex items-center space-x-2 mb-1">
                             <h3 className="text-sm font-medium text-gray-900 capitalize">
                               {transaction.transactionType.toLowerCase()}
                             </h3>
                           </div>
                          
                          <p className="text-sm text-gray-600 mb-2">{transaction.description}</p>
                          
                          <div className="flex items-center space-x-4 text-xs text-gray-500">
                            <span>Ref: {transaction.referenceNumber}</span>
                            {transaction.fromAccount && (
                              <span>From: {transaction.fromAccount}</span>
                            )}
                            {transaction.toAccount && (
                              <span>To: {transaction.toAccount}</span>
                            )}
                            <span>{new Date(transaction.createdAt).toLocaleDateString()}</span>
                          </div>
                        </div>
                      </div>
                      
                      <div className="text-right">
                        <p className={`text-lg font-bold ${
                          transaction.transactionType === 'DEPOSIT' ? 'text-green-600' : 'text-red-600'
                        }`}>
                          {transaction.transactionType === 'DEPOSIT' ? '+' : '-'}${transaction.amount.toFixed(2)}
                        </p>
                        <p className="text-sm text-gray-600">Balance: ${transaction.balanceAfter.toFixed(2)}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Transaction Form Modal */}
        {showTransactionForm && (
          <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
            <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
              <div className="mt-3">
                                 <h3 className="text-lg font-medium text-gray-900 mb-4">
                   {transactionType === 'DEPOSIT' ? 'Make a Deposit' : 
                    transactionType === 'WITHDRAWAL' ? 'Make a Withdrawal' : 'Make a Transfer'}
                 </h3>
                
                <form onSubmit={handleTransactionSubmit} className="space-y-4">
                                     <div>
                     <label className="block text-sm font-medium text-gray-700 mb-2">
                       {transactionType === 'TRANSFER' ? 'From Account' : 'Select Account'}
                     </label>
                     <select
                       value={selectedAccount}
                       onChange={(e) => setSelectedAccount(Number(e.target.value))}
                       required
                       className="input-field w-full"
                     >
                       <option value="">Choose an account</option>
                       {accounts.map((account) => (
                         <option key={account.id} value={account.id}>
                           {account.accountNumber} - {account.accountType} (${account.balance.toFixed(2)})
                         </option>
                       ))}
                     </select>
                   </div>
                   
                   {transactionType === 'TRANSFER' && (
                     <div>
                       <label className="block text-sm font-medium text-gray-700 mb-2">
                         To Account
                       </label>
                       <select
                         value={destinationAccount}
                         onChange={(e) => setDestinationAccount(Number(e.target.value))}
                         required
                         className="input-field w-full"
                       >
                         <option value="">Choose destination account</option>
                         {accounts.map((account) => (
                           <option key={account.id} value={account.id}>
                             {account.accountNumber} - {account.accountType} (${account.balance.toFixed(2)})
                           </option>
                         ))}
                       </select>
                     </div>
                   )}
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Amount
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="0.01"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      required
                      className="input-field w-full"
                      placeholder="Enter amount"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Description (Optional)
                    </label>
                    <input
                      type="text"
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      className="input-field w-full"
                      placeholder="Enter description"
                    />
                  </div>
                  
                  <div className="flex space-x-3 pt-4">
                                         <button
                       type="submit"
                       disabled={processingTransaction}
                       className="btn-primary flex-1"
                     >
                       {processingTransaction ? 'Processing...' : 
                        transactionType === 'DEPOSIT' ? 'Deposit' : 
                        transactionType === 'WITHDRAWAL' ? 'Withdraw' : 'Transfer'}
                     </button>
                    <button
                      type="button"
                      onClick={() => setShowTransactionForm(false)}
                      className="btn-secondary flex-1"
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}

        {/* Export Options */}
        <div className="mt-8 bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Export Transactions</h3>
          <div className="flex space-x-4">
            <button className="btn-secondary">
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Export to PDF
            </button>
            
            <button className="btn-secondary">
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Export to CSV
            </button>
            
            <button className="btn-secondary">
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Print Statement
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Transactions;
