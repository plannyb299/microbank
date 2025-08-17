import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { bankingService, Account, Transaction } from '../services/bankingService';

interface MonthlyData {
  month: string;
  deposits: number;
  withdrawals: number;
  transfers: number;
}

const Reports: React.FC = () => {
  const { user } = useAuth();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedPeriod, setSelectedPeriod] = useState('30');

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
        }
      } catch (error: any) {
        console.error('Error fetching data:', error);
        setError(error.message || 'Failed to load reports data');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [user?.id]);

  const getMonthlyData = (): MonthlyData[] => {
    const months: { [key: string]: MonthlyData } = {};
    const days = parseInt(selectedPeriod);
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - days);

    transactions
      .filter(t => new Date(t.createdAt) >= cutoffDate)
      .forEach(transaction => {
        const month = new Date(transaction.createdAt).toLocaleDateString('en-US', { 
          month: 'short', 
          year: 'numeric' 
        });
        
        if (!months[month]) {
          months[month] = { month, deposits: 0, withdrawals: 0, transfers: 0 };
        }
        
        switch (transaction.transactionType) {
          case 'DEPOSIT':
            months[month].deposits += transaction.amount;
            break;
          case 'WITHDRAWAL':
            months[month].withdrawals += transaction.amount;
            break;
          case 'TRANSFER':
            months[month].transfers += transaction.amount;
            break;
        }
      });

    return Object.values(months).sort((a, b) => 
      new Date(a.month).getTime() - new Date(b.month).getTime()
    );
  };

  const getTotalBalance = () => accounts.reduce((sum, acc) => sum + acc.balance, 0);
  const getTotalDeposits = () => transactions
    .filter(t => t.transactionType === 'DEPOSIT')
    .reduce((sum, t) => sum + t.amount, 0);
  const getTotalWithdrawals = () => transactions
    .filter(t => t.transactionType === 'WITHDRAWAL')
    .reduce((sum, t) => sum + t.amount, 0);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading reports...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 text-6xl mb-4">⚠️</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Something went wrong</h2>
          <p className="text-gray-600 mb-4">{error}</p>
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

  const monthlyData = getMonthlyData();

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Financial Reports</h1>
          <p className="text-gray-600">Track your banking activity and financial insights.</p>
        </div>

        {/* Period Selector */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">Time Period</label>
          <select
            value={selectedPeriod}
            onChange={(e) => setSelectedPeriod(e.target.value)}
            className="input-field w-48"
          >
            <option value="7">Last 7 days</option>
            <option value="30">Last 30 days</option>
            <option value="90">Last 90 days</option>
            <option value="365">Last year</option>
          </select>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 rounded-lg">
                <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Balance</p>
                <p className="text-2xl font-bold text-gray-900">${getTotalBalance().toFixed(2)}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 rounded-lg">
                <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Active Accounts</p>
                <p className="text-2xl font-bold text-gray-900">{accounts.length}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 rounded-lg">
                <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Deposits</p>
                <p className="text-2xl font-bold text-gray-900">${getTotalDeposits().toFixed(2)}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-red-100 rounded-lg">
                <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Withdrawals</p>
                <p className="text-2xl font-bold text-gray-900">${getTotalWithdrawals().toFixed(2)}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Monthly Activity Chart */}
        <div className="bg-white rounded-lg shadow p-6 mb-8">
          <h2 className="text-lg font-medium text-gray-900 mb-4">Monthly Activity</h2>
          {monthlyData.length === 0 ? (
            <p className="text-gray-500 text-center py-8">No activity in the selected period.</p>
          ) : (
            <div className="space-y-4">
              {monthlyData.map((data, index) => (
                <div key={index} className="border border-gray-200 rounded-lg p-4">
                  <h3 className="font-medium text-gray-900 mb-3">{data.month}</h3>
                  <div className="grid grid-cols-3 gap-4">
                    <div className="text-center">
                      <p className="text-sm text-gray-600">Deposits</p>
                      <p className="text-lg font-bold text-green-600">${data.deposits.toFixed(2)}</p>
                    </div>
                    <div className="text-center">
                      <p className="text-sm text-gray-600">Withdrawals</p>
                      <p className="text-lg font-bold text-red-600">${data.withdrawals.toFixed(2)}</p>
                    </div>
                    <div className="text-center">
                      <p className="text-sm text-gray-600">Transfers</p>
                      <p className="text-lg font-bold text-blue-600">${data.transfers.toFixed(2)}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Account Breakdown */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">Account Breakdown</h2>
          {accounts.length === 0 ? (
            <p className="text-gray-500 text-center py-8">No accounts found.</p>
          ) : (
            <div className="space-y-4">
              {accounts.map((account) => (
                <div key={account.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="font-medium text-gray-900">{account.accountNumber}</p>
                      <p className="text-sm text-gray-600 capitalize">{account.accountType.toLowerCase()}</p>
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        account.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                      }`}>
                        {account.status}
                      </span>
                    </div>
                    <div className="text-right">
                      <p className="text-2xl font-bold text-gray-900">${account.balance.toFixed(2)}</p>
                      <p className="text-sm text-gray-600">
                        {((account.balance / getTotalBalance()) * 100).toFixed(1)}% of total
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Reports;
