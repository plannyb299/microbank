import axios from 'axios';
import { extractErrorMessage } from '../utils/errorHandler';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8083';

// Create axios instance with auth interceptor
const auditApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include JWT token
auditApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface AuditLog {
  id: number;
  userId: number | null;
  userEmail: string;
  userRole: string;
  clientId: number | null;
  entityType: string;
  entityId: number | null;
  action: string;
  actionDetails: string;
  oldValues: string | null;
  newValues: string | null;
  ipAddress: string | null;
  userAgent: string | null;
  sessionId: string | null;
  requestId: string | null;
  status: string;
  failureReason: string | null;
  createdAt: string;
}

export interface AuditStatistics {
  totalEvents: number;
  securityEvents: number;
  failedEvents: number;
  successRate: number;
  startDate: string;
  endDate: string;
  generatedAt: string;
}

export interface AuditSearchFilters {
  searchTerm?: string;
  entityType?: string;
  action?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export class AuditService {
  // Get all audit logs with pagination
  async getAllAuditLogs(page: number = 0, size: number = 20): Promise<{ content: AuditLog[]; totalElements: number; totalPages: number }> {
    try {
      const response = await auditApi.get(`/api/v1/audit/logs?page=${page}&size=${size}`);
      return {
        content: response.data.content,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages
      };
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }

  // Search audit logs with filters
  async searchAuditLogs(filters: AuditSearchFilters): Promise<{ content: AuditLog[]; totalElements: number; totalPages: number }> {
    try {
      const params = new URLSearchParams();
      if (filters.searchTerm) params.append('searchTerm', filters.searchTerm);
      if (filters.entityType) params.append('entityType', filters.entityType);
      if (filters.action) params.append('action', filters.action);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.page !== undefined) params.append('page', filters.page.toString());
      if (filters.size !== undefined) params.append('size', filters.size.toString());

      const response = await auditApi.get(`/api/v1/audit/logs/search?${params.toString()}`);
      return {
        content: response.data.content,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages
      };
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }

  // Get recent audit logs
  async getRecentAuditLogs(): Promise<AuditLog[]> {
    try {
      const response = await auditApi.get('/api/v1/audit/logs/recent');
      return response.data;
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }

  // Get security audit logs
  async getSecurityAuditLogs(page: number = 0, size: number = 20): Promise<{ content: AuditLog[]; totalElements: number; totalPages: number }> {
    try {
      const response = await auditApi.get(`/api/v1/audit/logs/security?page=${page}&size=${size}`);
      return {
        content: response.data.content,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages
      };
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }

  // Get failed audit logs
  async getFailedAuditLogs(page: number = 0, size: number = 20): Promise<{ content: AuditLog[]; totalElements: number; totalPages: number }> {
    try {
      const response = await auditApi.get(`/api/v1/audit/logs/failed?page=${page}&size=${size}`);
      return {
        content: response.data.content,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages
      };
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }

  // Get audit statistics
  async getAuditStatistics(startDate?: string, endDate?: string): Promise<AuditStatistics> {
    try {
      const params = new URLSearchParams();
      if (startDate) params.append('startDate', startDate);
      if (endDate) params.append('endDate', endDate);

      const response = await auditApi.get(`/api/v1/audit/statistics/summary?${params.toString()}`);
      return response.data;
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }

  // Export audit logs to CSV
  async exportAuditLogsToCsv(startDate: string, endDate: string): Promise<Blob> {
    try {
      const response = await auditApi.get(`/api/v1/audit/export/csv?startDate=${startDate}&endDate=${endDate}`, {
        responseType: 'blob'
      });
      return response.data;
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }

  // Generate compliance report
  async generateComplianceReport(startDate: string, endDate: string): Promise<string> {
    try {
      const response = await auditApi.get(`/api/v1/audit/report/compliance?startDate=${startDate}&endDate=${endDate}`);
      return response.data;
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }

  // Generate daily summary report
  async generateDailySummaryReport(): Promise<string> {
    try {
      const endDate = new Date().toISOString();
      const startDate = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(); // Last 24 hours
      const response = await auditApi.get(`/api/v1/audit/report/compliance?startDate=${startDate}&endDate=${endDate}`);
      return response.data;
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }

  // Generate security events report
  async generateSecurityEventsReport(): Promise<string> {
    try {
      const endDate = new Date().toISOString();
      const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(); // Last 30 days
      const response = await auditApi.get(`/api/v1/audit/report/compliance?startDate=${startDate}&endDate=${endDate}`);
      return response.data;
    } catch (error: any) {
      throw new Error(extractErrorMessage(error));
    }
  }
}

export const auditService = new AuditService();
