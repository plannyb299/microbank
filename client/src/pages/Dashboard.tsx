import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { bankingService, Account, Transaction } from '../services/bankingService';
import { extractErrorMessage, isClientAccessError } from '../utils/errorHandler';

// Remove duplicate interfaces since we're importing them

const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [recentTransactions, setRecentTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [creatingAccount, setCreatingAccount] = useState(false);
  const [isAccountSuspended, setIsAccountSuspended] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        if (user?.id) {
          // Only fetch banking data for CLIENT users
          if (user.role === 'CLIENT') {
            const [accountsData, transactionsData] = await Promise.all([
              bankingService.getAccountsByClient(user.id),
              bankingService.getTransactionsByClient(user.id)
            ]);
            
            setAccounts(accountsData);
            setRecentTransactions(transactionsData.slice(0, 5)); // Show last 5 transactions
          } else if (user.role === 'ADMIN') {
            // For admin users, don't fetch banking data - they manage, don't bank
            setAccounts([]);
            setRecentTransactions([]);
            setLoading(false); // Admin dashboard loads immediately
            return; // Exit early for admin users
          }
        }
      } catch (error: any) {
        console.error('Error fetching data:', error);
        const errorMessage = extractErrorMessage(error);
        
        // Check if this is a client access error (likely blacklisted)
        if (isClientAccessError({ response: { data: { errorCode: errorMessage } } })) {
          // For blacklisted clients, show a suspension notice but don't block the dashboard
          setIsAccountSuspended(true);
          setError(''); // Clear error to show dashboard
        } else {
          // For other errors, show the error screen
          setError(errorMessage);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [user?.id, user?.role]);

  const handleCreateAccount = async (accountType: string) => {
    if (!user?.id) return;
    
    setCreatingAccount(true);
    try {
      const newAccount = await bankingService.createAccount(user.id, accountType);
      setAccounts(prev => [...prev, newAccount]);
      setError('');
    } catch (error: any) {
      setError(extractErrorMessage(error));
    } finally {
      setCreatingAccount(false);
    }
  };

  const handleQuickAction = (action: string) => {
    // Prevent actions for suspended accounts
    if (isAccountSuspended && (action === 'deposit' || action === 'withdraw' || action === 'transfer')) {
      return; // Do nothing for suspended accounts
    }
    
    // Navigate to appropriate page using React Router
    switch (action) {
      case 'deposit':
        navigate('/transactions?action=deposit');
        break;
      case 'withdraw':
        navigate('/transactions?action=withdraw');
        break;
      case 'transfer':
        navigate('/transactions?action=transfer');
        break;
      case 'reports':
        navigate('/reports');
        break;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading your dashboard...</p>
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
            {isAccessError ? 'Access Restricted' : 'Error Loading Dashboard'}
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

  const totalBalance = Array.isArray(accounts) ? accounts.reduce((sum, account) => sum + account.balance, 0) : 0;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">
            Welcome back, {user?.firstName}!
          </h1>
          <p className="text-gray-600">
            {user?.role === 'ADMIN' 
              ? 'Here\'s your admin dashboard for managing the banking system. You can manage clients, monitor system health, and oversee operations.'
              : 'Here\'s what\'s happening with your accounts today.'
            }
          </p>
        </div>

        {/* Account Suspension Notice */}
        {isAccountSuspended && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
            <div className="flex items-start">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
              </div>
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">
                  Account Temporarily Suspended
                </h3>
                <div className="mt-2 text-sm text-red-700">
                  <p>
                    Your account has been temporarily suspended. You can still view your account information, 
                    but you cannot perform any transactions. Please contact customer support for assistance.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Show banking content only for CLIENT users */}
        {user?.role === 'CLIENT' && (
          <>
            {/* Client Banking Notice */}
            <div className="mb-6 bg-green-50 border border-green-200 rounded-lg p-4">
              <div className="flex items-start">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-green-800">
                    Client Banking Access
                  </h3>
                  <div className="mt-2 text-sm text-green-700">
                    <p>
                      As a client, you have access to your banking information and can perform transactions. 
                      Use the quick actions below to manage your accounts.
                    </p>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Summary Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-green-100 rounded-lg">
                    <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                    </svg>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600">Total Balance</p>
                    <p className="text-2xl font-bold text-gray-900">${totalBalance.toFixed(2)}</p>
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
                    <p className="text-2xl font-bold text-gray-900">{Array.isArray(accounts) ? accounts.length : 0}</p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-purple-100 rounded-lg">
                    <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600">Recent Transactions</p>
                    <p className="text-2xl font-bold text-gray-900">{Array.isArray(recentTransactions) ? recentTransactions.length : 0}</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Accounts Section */}
              <div className="bg-white rounded-lg shadow">
                <div className="px-6 py-4 border-b border-gray-200">
                  <h2 className="text-lg font-medium text-gray-900">Your Accounts</h2>
                </div>
                <div className="p-6">
                  {!Array.isArray(accounts) || accounts.length === 0 ? (
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
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Recent Transactions Section */}
              <div className="bg-white rounded-lg shadow">
                <div className="px-6 py-4 border-b border-gray-200">
                  <h2 className="text-lg font-medium text-gray-900">Recent Transactions</h2>
                </div>
                <div className="p-6">
                  {!Array.isArray(recentTransactions) || recentTransactions.length === 0 ? (
                    <p className="text-gray-500 text-center py-8">No recent transactions.</p>
                  ) : (
                    <div className="space-y-4">
                      {recentTransactions.map((transaction) => (
                        <div key={transaction.id} className="border border-gray-200 rounded-lg p-4">
                          <div className="flex justify-between items-start">
                            <div>
                              <p className="font-medium text-gray-900 capitalize">{transaction.transactionType.toLowerCase()}</p>
                              <p className="text-sm text-gray-600">{transaction.description}</p>
                              <p className="text-xs text-gray-500">
                                {new Date(transaction.createdAt).toLocaleDateString()}
                              </p>
                            </div>
                            <div className="text-right">
                              <p className={`font-medium ${
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
            </div>

                        {/* Create Account Section - Only for CLIENT users */}
            {(!Array.isArray(accounts) || accounts.length === 0) && user?.role === 'CLIENT' && (
              <div className="mt-8 bg-white rounded-lg shadow p-6">
                <h2 className="text-lg font-medium text-gray-900 mb-4">Get Started</h2>
                <p className="text-gray-600 mb-4">You don't have any accounts yet. Create your first account to start banking.</p>
                {isAccountSuspended && (
                  <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                    <p className="text-sm text-yellow-800">
                      <strong>Note:</strong> Account creation is disabled due to account suspension.
                    </p>
                  </div>
                )}
                <div className="space-y-4">
                  <button 
                    onClick={() => handleCreateAccount('SAVINGS')}
                    disabled={creatingAccount || isAccountSuspended}
                    className={`mr-4 ${isAccountSuspended ? 'btn-disabled' : 'btn-primary'}`}
                  >
                    {creatingAccount ? 'Creating...' : 'Create Savings Account'}
                  </button>
                  <button 
                    onClick={() => handleCreateAccount('CHECKING')}
                    disabled={creatingAccount || isAccountSuspended}
                    className={isAccountSuspended ? 'btn-disabled' : 'btn-secondary'}
                  >
                    {creatingAccount ? 'Creating...' : 'Create Checking Account'}
                  </button>
                </div>
              </div>
            )}

            {/* Quick Actions - Only for CLIENT users */}
            {accounts.length > 0 && user?.role === 'CLIENT' && (
              <div className="mt-8 bg-white rounded-lg shadow p-6">
                <h2 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h2>
                {isAccountSuspended && (
                  <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                    <p className="text-sm text-yellow-800">
                      <strong>Note:</strong> Transaction actions are disabled due to account suspension.
                    </p>
                  </div>
                )}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <button 
                    onClick={() => handleQuickAction('deposit')}
                    disabled={isAccountSuspended}
                    className={`flex flex-col items-center p-4 border rounded-lg transition-colors ${
                      isAccountSuspended 
                        ? 'border-gray-300 bg-gray-100 cursor-not-allowed opacity-50' 
                        : 'border-gray-200 hover:bg-gray-50'
                    }`}
                  >
                    <svg className={`w-8 h-8 mb-2 ${isAccountSuspended ? 'text-gray-400' : 'text-green-600'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                    </svg>
                    <span className={`text-sm font-medium ${isAccountSuspended ? 'text-gray-500' : 'text-gray-900'}`}>Deposit</span>
                  </button>
                  
                  <button 
                    onClick={() => handleQuickAction('withdraw')}
                    disabled={isAccountSuspended}
                    className={`flex flex-col items-center p-4 border rounded-lg transition-colors ${
                      isAccountSuspended 
                        ? 'border-gray-300 bg-gray-100 cursor-not-allowed opacity-50' 
                        : 'border-gray-200 hover:bg-gray-50'
                    }`}
                  >
                    <svg className={`w-8 h-8 mb-2 ${isAccountSuspended ? 'text-gray-400' : 'text-red-600'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                    </svg>
                    <span className={`text-sm font-medium ${isAccountSuspended ? 'text-gray-500' : 'text-gray-900'}`}>Withdraw</span>
                  </button>
                  
                  <button 
                    onClick={() => handleQuickAction('transfer')}
                    disabled={isAccountSuspended}
                    className={`flex flex-col items-center p-4 border rounded-lg transition-colors ${
                      isAccountSuspended 
                        ? 'border-gray-300 bg-gray-100 cursor-not-allowed opacity-50' 
                        : 'border-gray-200 hover:bg-gray-50'
                    }`}
                  >
                    <svg className={`w-8 h-8 mb-2 ${isAccountSuspended ? 'text-gray-400' : 'text-blue-600'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                    </svg>
                    <span className={`text-sm font-medium ${isAccountSuspended ? 'text-gray-500' : 'text-gray-900'}`}>Transfer</span>
                  </button>
                  
                  <button 
                    onClick={() => handleQuickAction('reports')}
                    className="flex flex-col items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    <svg className="w-8 h-8 text-purple-600 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                    <span className="text-sm font-medium text-gray-900">Reports</span>
                  </button>
                </div>
              </div>
            )}
          </>
        )}

                    {/* Admin Dashboard Section */}
        {user?.role === 'ADMIN' && (
          <div className="mt-8 bg-white rounded-lg shadow p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">Admin Dashboard</h2>
            <p className="text-gray-600 mb-4">Welcome to the admin panel. You have access to system management functions only.</p>
            
            {/* Admin Restrictions Notice */}
            <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <div className="flex items-start">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-blue-800">
                    Administrative Access Only
                  </h3>
                  <div className="mt-2 text-sm text-blue-700">
                    <p>
                      <strong>What you CAN do:</strong> Manage clients, monitor system health, view audit logs, oversee operations
                    </p>
                    <p className="mt-1">
                      <strong>What you CANNOT do:</strong> Create accounts, perform deposits/withdrawals/transfers, access banking services
                    </p>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
              <button 
                onClick={() => navigate('/admin/clients')}
                className="flex flex-col items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <svg className="w-8 h-8 text-blue-600 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <span className="text-sm font-medium text-gray-900">Client Management</span>
              </button>
              
              <button 
                onClick={() => navigate('/audit')}
                className="flex flex-col items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <svg className="w-8 h-8 text-green-600 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <span className="text-sm font-medium text-gray-900">Audit Logs</span>
              </button>
              

              
              <button 
                onClick={() => navigate('/admin/health')}
                className="flex flex-col items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <svg className="w-8 h-8 text-red-600 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
                <span className="text-sm font-medium text-gray-900">System Health</span>
              </button>
            </div>
            
            {/* Additional Admin Info */}
            <div className="mt-6 p-4 bg-gray-50 border border-gray-200 rounded-lg">
              <h3 className="text-sm font-medium text-gray-900 mb-2">Your Role Responsibilities:</h3>
              <ul className="text-sm text-gray-600 space-y-1">
                <li>• <strong>Client Management:</strong> Create, update, and manage client accounts</li>
                <li>• <strong>System Monitoring:</strong> Monitor system health and performance</li>
                <li>• <strong>Audit & Compliance:</strong> Review logs and ensure regulatory compliance</li>
                <li>• <strong>Support:</strong> Assist clients with account-related issues</li>
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
