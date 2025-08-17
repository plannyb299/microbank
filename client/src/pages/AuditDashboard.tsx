import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { auditService, AuditLog, AuditStatistics, AuditSearchFilters } from '../services/auditService';
import { extractErrorMessage, isClientAccessError } from '../utils/errorHandler';

const AuditDashboard: React.FC = () => {
  const { user } = useAuth();
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [statistics, setStatistics] = useState<AuditStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'overview' | 'logs' | 'security' | 'reports'>('overview');
  
  // Filter states
  const [filters, setFilters] = useState<AuditSearchFilters>({
    searchTerm: '',
    entityType: '',
    action: '',
    startDate: '',
    endDate: ''
  });
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    if (activeTab === 'overview') {
      fetchStatistics();
    } else if (activeTab === 'logs') {
      fetchAuditLogs();
    } else if (activeTab === 'security') {
      fetchSecurityLogs();
    }
  }, [activeTab, currentPage, pageSize, filters]);

  const fetchStatistics = async () => {
    try {
      setLoading(true);
      const stats = await auditService.getAuditStatistics();
      setStatistics(stats);
    } catch (err: any) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const fetchAuditLogs = async () => {
    try {
      setLoading(true);
      // Get real audit logs from API
      const result = await auditService.getAllAuditLogs(currentPage, pageSize);
      setAuditLogs(result.content);
      setTotalPages(result.totalPages);
    } catch (err: any) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const fetchSecurityLogs = async () => {
    try {
      setLoading(true);
      const securityLogs = await auditService.getSecurityAuditLogs(currentPage, pageSize);
      setAuditLogs(securityLogs.content);
      setTotalPages(securityLogs.totalPages);
    } catch (err: any) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (key: string, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setCurrentPage(0);
  };

  const clearFilters = () => {
    setFilters({
      searchTerm: '',
      entityType: '',
      action: '',
      startDate: '',
      endDate: ''
    });
    setCurrentPage(0);
  };

  const exportToCsv = () => {
    // Implementation for CSV export
    const csvContent = auditLogs.map(log => 
      `${log.id},${log.userEmail},${log.action},${log.entityType},${log.status},${log.createdAt}`
    ).join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit_logs_${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  };

  // Handle daily report generation
  const handleGenerateDailyReport = async () => {
    try {
      setLoading(true);
      setError('');
      const report = await auditService.generateDailySummaryReport();
      // For now, we'll use the statistics as a placeholder for the report
      // In a real implementation, you might want to display the actual report text
      await fetchStatistics();
    } catch (err: any) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  // Handle security report generation
  const handleGenerateSecurityReport = async () => {
    try {
      setLoading(true);
      setError('');
      const report = await auditService.generateSecurityEventsReport();
      // For now, we'll use the statistics as a placeholder for the report
      await fetchStatistics();
    } catch (err: any) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  // Handle compliance report generation
  const handleGenerateComplianceReport = async () => {
    try {
      setLoading(true);
      setError('');
      const endDate = new Date().toISOString();
      const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(); // Last 30 days
      const report = await auditService.generateComplianceReport(startDate, endDate);
      // For now, we'll use the statistics as a placeholder for the report
      await fetchStatistics();
    } catch (err: any) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  // Handle data export
  const handleExportData = async () => {
    try {
      setLoading(true);
      setError('');
      const endDate = new Date().toISOString();
      const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(); // Last 30 days
      const blob = await auditService.exportAuditLogsToCsv(startDate, endDate);
      
      // Download the CSV file
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `audit_logs_${startDate.split('T')[0]}_to_${endDate.split('T')[0]}.csv`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (err: any) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const getActionIcon = (action: string) => {
    switch (action) {
      case 'CREATE': return '➕';
      case 'UPDATE': return '✏️';
      case 'DELETE': return '🗑️';
      case 'READ': return '👁️';
      case 'LOGIN': return '🔑';
      case 'LOGOUT': return '🚪';
      case 'TRANSFER': return '💸';
      case 'WITHDRAWAL': return '💳';
      case 'DEPOSIT': return '💰';
      default: return '📝';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading audit data...</p>
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
            {isAccessError ? 'Access Restricted' : 'Error Loading Audit Data'}
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
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Audit Dashboard</h1>
          <p className="mt-2 text-gray-600">
            Comprehensive audit trail and compliance monitoring for the Microbank platform
          </p>
        </div>

        {/* Tabs */}
        <div className="border-b border-gray-200 mb-6">
          <nav className="-mb-px flex space-x-8">
            {[
              { id: 'overview', name: 'Overview', icon: '📊' },
              { id: 'logs', name: 'Audit Logs', icon: '📋' },
              { id: 'security', name: 'Security Events', icon: '🔒' },
              { id: 'reports', name: 'Reports', icon: '📈' }
            ].map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <span className="mr-2">{tab.icon}</span>
                {tab.name}
              </button>
            ))}
          </nav>
        </div>

        {/* Tab Content */}
        {activeTab === 'overview' && statistics && (
          <div className="space-y-6">
            {/* Statistics Cards */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="bg-white overflow-hidden shadow rounded-lg">
                <div className="p-5">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="text-2xl">📊</div>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="text-sm font-medium text-gray-500 truncate">Total Events</dt>
                        <dd className="text-lg font-medium text-gray-900">{statistics.totalEvents.toLocaleString()}</dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>

              <div className="bg-white overflow-hidden shadow rounded-lg">
                <div className="p-5">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="text-2xl">🔒</div>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="text-sm font-medium text-gray-500 truncate">Security Events</dt>
                        <dd className="text-lg font-medium text-gray-900">{statistics.securityEvents}</dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>

              <div className="bg-white overflow-hidden shadow rounded-lg">
                <div className="p-5">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="text-2xl">❌</div>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="text-sm font-medium text-gray-500 truncate">Failed Events</dt>
                        <dd className="text-lg font-medium text-gray-900">{statistics.failedEvents}</dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>

              <div className="bg-white overflow-hidden shadow rounded-lg">
                <div className="p-5">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="text-2xl">✅</div>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="text-sm font-medium text-gray-500 truncate">Success Rate</dt>
                        <dd className="text-lg font-medium text-gray-900">{statistics.successRate.toFixed(2)}%</dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Recent Activity */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-4 py-5 sm:p-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Recent Audit Activity</h3>
                <div className="space-y-3">
                  {auditLogs.slice(0, 5).map(log => (
                    <div key={log.id} className="flex items-center space-x-3 p-3 bg-gray-50 rounded">
                      <span className="text-lg">{getActionIcon(log.action)}</span>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {log.action} - {log.entityType}
                        </p>
                        <p className="text-sm text-gray-500 truncate">
                          by {log.userEmail} • {new Date(log.createdAt).toLocaleString()}
                        </p>
                      </div>
                      {/* Removed getStatusBadge */}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {(activeTab === 'logs' || activeTab === 'security') && (
          <div className="space-y-6">
            {/* Filters */}
            <div className="bg-white shadow rounded-lg p-6">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <input
                  type="text"
                  placeholder="Search..."
                  value={filters.searchTerm}
                  onChange={(e) => handleFilterChange('searchTerm', e.target.value)}
                  className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <select
                  value={filters.entityType}
                  onChange={(e) => handleFilterChange('entityType', e.target.value)}
                  className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">All Entity Types</option>
                  <option value="ACCOUNT">Account</option>
                  <option value="TRANSACTION">Transaction</option>
                  <option value="CLIENT">Client</option>
                  <option value="SECURITY_EVENT">Security Event</option>
                </select>
              </div>
              <div className="mt-4 flex space-x-4">
                <button
                  onClick={clearFilters}
                  className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                >
                  Clear Filters
                </button>
                <button
                  onClick={exportToCsv}
                  className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
                >
                  Export CSV
                </button>
              </div>
            </div>

            {/* Audit Logs Table */}
            <div className="bg-white shadow rounded-lg overflow-hidden">
              <div className="px-4 py-5 sm:p-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
                  {activeTab === 'logs' ? 'Audit Logs' : 'Security Events'}
                </h3>
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Action
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          User
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Entity
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Details
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          IP Address
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Timestamp
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {auditLogs.map(log => (
                        <tr key={log.id} className="hover:bg-gray-50">
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="flex items-center">
                              <span className="text-lg mr-2">{getActionIcon(log.action)}</span>
                              <span className="text-sm font-medium text-gray-900">{log.action}</span>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div>
                              <div className="text-sm font-medium text-gray-900">{log.userEmail}</div>
                              <div className="text-sm text-gray-500">{log.userRole}</div>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div>
                              <div className="text-sm font-medium text-gray-900">{log.entityType}</div>
                              {log.entityId && (
                                <div className="text-sm text-gray-500">ID: {log.entityId}</div>
                              )}
                            </div>
                          </td>
                          <td className="px-6 py-4">
                            <div className="text-sm text-gray-900 max-w-xs truncate" title={log.actionDetails}>
                              {log.actionDetails}
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {log.ipAddress}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {new Date(log.createdAt).toLocaleString()}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Pagination */}
                <div className="mt-6 flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <span className="text-sm text-gray-700">Show</span>
                    <select
                      value={pageSize}
                      onChange={(e) => setPageSize(Number(e.target.value))}
                      className="border border-gray-300 rounded-md px-2 py-1 text-sm"
                    >
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </select>
                    <span className="text-sm text-gray-700">entries</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                      disabled={currentPage === 0}
                      className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50"
                    >
                      Previous
                    </button>
                    <span className="text-sm text-gray-700">
                      Page {currentPage + 1} of {totalPages}
                    </span>
                    <button
                      onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                      disabled={currentPage === totalPages - 1}
                      className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50"
                    >
                      Next
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'reports' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Compliance Reports</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="border border-gray-200 rounded-lg p-4">
                  <h4 className="font-medium text-gray-900 mb-2">Daily Summary Report</h4>
                  <p className="text-sm text-gray-600 mb-4">Generate a daily summary of all audit activities</p>
                  <button 
                    onClick={() => handleGenerateDailyReport()}
                    disabled={loading}
                    className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Generating...' : 'Generate Report'}
                  </button>
                </div>
                <div className="border border-gray-200 rounded-lg p-4">
                  <h4 className="font-medium text-gray-900 mb-2">Security Events Report</h4>
                  <p className="text-sm text-gray-600 mb-4">Detailed report of security-related events</p>
                  <button 
                    onClick={() => handleGenerateSecurityReport()}
                    disabled={loading}
                    className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Generating...' : 'Generate Report'}
                  </button>
                </div>
                <div className="border border-gray-200 rounded-lg p-4">
                  <h4 className="font-medium text-gray-900 mb-2">Compliance Report</h4>
                  <p className="text-sm text-gray-600 mb-4">Full compliance audit report for regulatory purposes</p>
                  <button 
                    onClick={() => handleGenerateComplianceReport()}
                    disabled={loading}
                    className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Generating...' : 'Generate Report'}
                  </button>
                </div>
                <div className="border border-gray-200 rounded-lg p-4">
                  <h4 className="font-medium text-gray-900 mb-2">Data Export</h4>
                  <p className="text-sm text-gray-600 mb-4">Export audit data in various formats</p>
                  <button 
                    onClick={() => handleExportData()}
                    disabled={loading}
                    className="bg-orange-600 text-white px-4 py-2 rounded hover:bg-orange-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Exporting...' : 'Export Data'}
                  </button>
                </div>
              </div>
              
              {/* Report Display Area */}
              {error && (
                <div className="mt-6 p-4 bg-red-50 border border-red-200 rounded-md">
                  <p className="text-red-800">{error}</p>
                </div>
              )}
              
              {/* Report Content Display */}
              {statistics && (
                <div className="mt-6 p-4 bg-gray-50 border border-gray-200 rounded-md">
                  <h4 className="font-medium text-gray-900 mb-2">Generated Report</h4>
                  <pre className="text-sm text-gray-700 whitespace-pre-wrap bg-white p-3 rounded border">
                    {JSON.stringify(statistics, null, 2)}
                  </pre>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AuditDashboard;
