import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { adminService, AdminClient } from '../services/adminService';

// Remove duplicate interface since we're importing AdminClient

interface Account {
  id: number;
  accountNumber: string;
  balance: number;
  accountType: string;
  status: string;
  clientId: number;
}

const Admin: React.FC = () => {
  const { user } = useAuth();
  const [clients, setClients] = useState<AdminClient[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'clients' | 'accounts' | 'statistics'>('clients');
  
  // Admin state
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredClients, setFilteredClients] = useState<AdminClient[]>([]);
  const [statistics, setStatistics] = useState({
    totalClients: 0,
    activeClients: 0,
    blacklistedClients: 0,
    suspendedClients: 0
  });
  
  // Blacklist modal state
  const [showBlacklistModal, setShowBlacklistModal] = useState(false);
  const [selectedClient, setSelectedClient] = useState<AdminClient | null>(null);
  const [blacklistReason, setBlacklistReason] = useState('');
  const [processingAction, setProcessingAction] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [clientsData, statisticsData] = await Promise.all([
          adminService.getAllClients(),
          adminService.getClientStatistics()
        ]);
        
        setClients(clientsData);
        setFilteredClients(clientsData);
        setStatistics(statisticsData);
      } catch (error: any) {
        console.error('Error fetching admin data:', error);
        setError(error.message || 'Failed to load admin data');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Filter clients based on search term
  useEffect(() => {
    if (searchTerm.trim() === '') {
      setFilteredClients(clients);
    } else {
      const filtered = clients.filter(client =>
        client.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        client.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        client.email.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredClients(filtered);
    }
  }, [clients, searchTerm]);

  const handleBlacklistClient = async () => {
    if (!selectedClient || !blacklistReason.trim()) return;
    
    setProcessingAction(true);
    try {
      const updatedClient = await adminService.blacklistClient({
        clientId: selectedClient.id,
        reason: blacklistReason
      });
      
      // Update the client in the list
      setClients(prev => prev.map(client => 
        client.id === selectedClient.id ? updatedClient : client
      ));
      
      // Close modal and reset
      setShowBlacklistModal(false);
      setSelectedClient(null);
      setBlacklistReason('');
      setError('');
      
      // Refresh statistics
      const newStats = await adminService.getClientStatistics();
      setStatistics(newStats);
      
    } catch (error: any) {
      setError(error.message || 'Failed to blacklist client');
    } finally {
      setProcessingAction(false);
    }
  };

  const handleUnblacklistClient = async (clientId: number) => {
    setProcessingAction(true);
    try {
      const updatedClient = await adminService.unblacklistClient({ clientId });
      
      // Update the client in the list
      setClients(prev => prev.map(client => 
        client.id === clientId ? updatedClient : client
      ));
      
      setError('');
      
      // Refresh statistics
      const newStats = await adminService.getClientStatistics();
      setStatistics(newStats);
      
    } catch (error: any) {
      setError(error.message || 'Failed to unblacklist client');
    } finally {
      setProcessingAction(false);
    }
  };

  const openBlacklistModal = (client: AdminClient) => {
    setSelectedClient(client);
    setBlacklistReason('');
    setShowBlacklistModal(true);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading admin panel...</p>
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

  const totalClients = clients.length;
  const activeClients = clients.filter(client => client.status === 'ACTIVE').length;
  const totalAccounts = accounts.length;
  const totalBalance = accounts.reduce((sum, account) => sum + account.balance, 0);

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Admin Panel</h1>
          <p className="text-gray-600">Manage clients, accounts, and view system statistics.</p>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 rounded-lg">
                <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Clients</p>
                <p className="text-2xl font-bold text-gray-900">{totalClients}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 rounded-lg">
                <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Active Clients</p>
                <p className="text-2xl font-bold text-gray-900">{activeClients}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-purple-100 rounded-lg">
                <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Accounts</p>
                <p className="text-2xl font-bold text-gray-900">{totalAccounts}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-yellow-100 rounded-lg">
                <svg className="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Balance</p>
                <p className="text-2xl font-bold text-gray-900">${totalBalance.toFixed(2)}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8 px-6">
              <button
                onClick={() => setActiveTab('clients')}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'clients'
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Clients
              </button>
              <button
                onClick={() => setActiveTab('accounts')}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'accounts'
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Accounts
              </button>
              <button
                onClick={() => setActiveTab('statistics')}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'statistics'
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Statistics
              </button>
            </nav>
          </div>

          <div className="p-6">
            {/* Clients Tab */}
            {activeTab === 'clients' && (
              <div>
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-lg font-medium text-gray-900">Client Management</h2>
                  <div className="flex space-x-3">
                    <input
                      type="text"
                      placeholder="Search clients..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      className="input-field"
                    />
                    <button className="btn-primary">
                      <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                      </svg>
                      Add Client
                    </button>
                  </div>
                </div>
                
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Phone</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Blacklist</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {filteredClients.map((client) => (
                        <tr key={client.id} className={client.blacklisted ? 'bg-red-50' : ''}>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm font-medium text-gray-900">{client.firstName} {client.lastName}</div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{client.email}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{client.phone}</td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                              client.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                            }`}>
                              {client.status}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            {client.blacklisted ? (
                              <div className="space-y-1">
                                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                                  BLACKLISTED
                                </span>
                                {client.blacklistReason && (
                                  <div className="text-xs text-red-600 max-w-xs truncate" title={client.blacklistReason}>
                                    {client.blacklistReason}
                                  </div>
                                )}
                              </div>
                            ) : (
                              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                ACTIVE
                              </span>
                            )}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            <button className="text-primary-600 hover:text-primary-900 mr-3">Edit</button>
                            {client.blacklisted ? (
                              <button 
                                onClick={() => handleUnblacklistClient(client.id)}
                                disabled={processingAction}
                                className="text-green-600 hover:text-green-900"
                              >
                                {processingAction ? 'Processing...' : 'Unblacklist'}
                              </button>
                            ) : (
                              <button 
                                onClick={() => openBlacklistModal(client)}
                                className="text-red-600 hover:text-red-900"
                              >
                                Blacklist
                              </button>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* Accounts Tab */}
            {activeTab === 'accounts' && (
              <div>
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-lg font-medium text-gray-900">Account Management</h2>
                  <button className="btn-primary">
                    <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                    </svg>
                    Create Account
                  </button>
                </div>
                
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Account Number</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Balance</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {accounts.map((account) => (
                        <tr key={account.id}>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{account.accountNumber}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 capitalize">{account.accountType.toLowerCase()}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${account.balance.toFixed(2)}</td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                              account.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                            }`}>
                              {account.status}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            <button className="text-primary-600 hover:text-primary-900 mr-3">View</button>
                            <button className="text-red-600 hover:text-red-900">Close</button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* Statistics Tab */}
            {activeTab === 'statistics' && (
              <div>
                <h2 className="text-lg font-medium text-gray-900 mb-4">System Statistics</h2>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="bg-gray-50 rounded-lg p-6">
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Client Statistics</h3>
                    <div className="space-y-3">
                      <div className="flex justify-between">
                        <span className="text-gray-600">Total Clients:</span>
                        <span className="font-medium">{statistics.totalClients}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Active Clients:</span>
                        <span className="font-medium">{statistics.activeClients}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Blacklisted Clients:</span>
                        <span className="font-medium text-red-600">{statistics.blacklistedClients}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Suspended Clients:</span>
                        <span className="font-medium">{statistics.suspendedClients}</span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="bg-gray-50 rounded-lg p-6">
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Account Statistics</h3>
                    <div className="space-y-3">
                      <div className="flex justify-between">
                        <span className="text-gray-600">Total Accounts:</span>
                        <span className="font-medium">{totalAccounts}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Total Balance:</span>
                        <span className="font-medium">${totalBalance.toFixed(2)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Average Balance:</span>
                        <span className="font-medium">${(totalBalance / totalAccounts).toFixed(2)}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Blacklist Modal */}
      {showBlacklistModal && selectedClient && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Blacklist Client: {selectedClient.firstName} {selectedClient.lastName}
              </h3>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Reason for Blacklisting
                  </label>
                  <textarea
                    value={blacklistReason}
                    onChange={(e) => setBlacklistReason(e.target.value)}
                    required
                    rows={3}
                    className="input-field w-full"
                    placeholder="Enter reason for blacklisting this client..."
                  />
                </div>
                
                <div className="flex space-x-3 pt-4">
                  <button
                    onClick={handleBlacklistClient}
                    disabled={processingAction || !blacklistReason.trim()}
                    className="btn-primary flex-1"
                  >
                    {processingAction ? 'Processing...' : 'Blacklist Client'}
                  </button>
                  <button
                    onClick={() => {
                      setShowBlacklistModal(false);
                      setSelectedClient(null);
                      setBlacklistReason('');
                    }}
                    className="btn-secondary flex-1"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Admin;
