import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8083';

const adminApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
adminApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface AdminClient {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  status: string;
  blacklisted: boolean;
  blacklistReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface BlacklistRequest {
  clientId: number;
  reason: string;
}

export interface UnblacklistRequest {
  clientId: number;
}

class AdminService {
  // Get all clients (admin only)
  async getAllClients(): Promise<AdminClient[]> {
    const response = await adminApi.get<AdminClient[]>('/api/v1/admin/clients');
    return response.data;
  }

  // Get client by ID
  async getClientById(clientId: number): Promise<AdminClient> {
    const response = await adminApi.get<AdminClient>(`/api/v1/admin/clients/${clientId}`);
    return response.data;
  }

  // Blacklist a client
  async blacklistClient(request: BlacklistRequest): Promise<AdminClient> {
    const response = await adminApi.post<AdminClient>(`/api/v1/admin/clients/${request.clientId}/blacklist`, {
      reason: request.reason
    });
    return response.data;
  }

  // Unblacklist a client
  async unblacklistClient(request: UnblacklistRequest): Promise<AdminClient> {
    const response = await adminApi.post<AdminClient>(`/api/v1/admin/clients/${request.clientId}/unblacklist`);
    return response.data;
  }

  // Update client status
  async updateClientStatus(clientId: number, status: string): Promise<AdminClient> {
    const response = await adminApi.put<AdminClient>(`/api/v1/admin/clients/${clientId}/status`, {
      status
    });
    return response.data;
  }

  // Get client statistics
  async getClientStatistics(): Promise<{
    totalClients: number;
    activeClients: number;
    blacklistedClients: number;
    suspendedClients: number;
  }> {
    const response = await adminApi.get('/api/v1/admin/statistics/clients');
    return response.data;
  }

  // Search clients
  async searchClients(query: string): Promise<AdminClient[]> {
    const response = await adminApi.get<AdminClient[]>(`/api/v1/admin/clients/search?q=${encodeURIComponent(query)}`);
    return response.data;
  }
}

export const adminService = new AdminService();
