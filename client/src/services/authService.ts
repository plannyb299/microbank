import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8083';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token expiration - only for authenticated requests, not login attempts
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only auto-redirect if it's not a login/register request
    if (error.response?.status === 401 && 
        !error.config?.url?.includes('/login') && 
        !error.config?.url?.includes('/register')) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  name: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  client: {
    id: number;
    email: string;
    name: string;
    blacklisted: boolean;
    status: string;
    role?: 'CLIENT' | 'ADMIN';
  };
  expiresIn: number;
}

export interface ClientResponse {
  id: number;
  email: string;
  name: string;
  blacklisted: boolean;
  status: string;
  role?: 'CLIENT' | 'ADMIN';
  createdAt: string;
  updatedAt: string;
}

class AuthService {
  async login(email: string, password: string): Promise<LoginResponse> {
    try {
      const response = await api.post<LoginResponse>('/api/v1/clients/login', {
        email,
        password,
      });
      return response.data;
    } catch (error: any) {
      // Extract user-friendly error message from the backend
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status === 401) {
        throw new Error('Invalid email or password');
      } else if (error.response?.status === 403) {
        throw new Error('Account is blocked. Please contact support.');
      } else {
        throw new Error('Login failed. Please try again.');
      }
    }
  }

  async register(email: string, name: string, password: string): Promise<void> {
    try {
      await api.post('/api/v1/clients/register', {
        email,
        name,
        password,
      });
    } catch (error: any) {
      // Extract user-friendly error message from the backend
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status === 400) {
        throw new Error('Registration failed. Please check your information.');
      } else if (error.response?.status === 409) {
        throw new Error('Email already exists. Please use a different email.');
      } else {
        throw new Error('Registration failed. Please try again.');
      }
    }
  }

  async validateToken(token: string): Promise<{
    id: number;
    email: string;
    name: string;
    firstName: string;
    lastName: string;
    phone: string;
    role: 'CLIENT' | 'ADMIN';
    blacklisted: boolean;
  }> {
    // For now, we'll decode the JWT token to get user info
    // In a real app, you might want to call a validate endpoint
    try {
      const response = await api.get('/api/v1/clients/profile');
      const client = response.data;
      
      // Split name into firstName and lastName
      const nameParts = client.name.split(' ');
      const firstName = nameParts[0] || '';
      const lastName = nameParts.slice(1).join(' ') || '';
      
      return {
        id: client.id,
        email: client.email,
        name: client.name,
        firstName,
        lastName,
        phone: client.phone || '',
        role: client.role || 'CLIENT',
        blacklisted: client.blacklisted
      };
    } catch (error) {
      throw new Error('Invalid token');
    }
  }

  async getProfile(): Promise<ClientResponse> {
    const response = await api.get<ClientResponse>('/api/v1/clients/profile');
    return response.data;
  }

  async updateProfile(email: string, name: string, password?: string): Promise<ClientResponse> {
    const data: any = { email, name };
    if (password) {
      data.password = password;
    }
    
    const response = await api.put<ClientResponse>('/api/v1/clients/profile', data);
    return response.data;
  }
}

export const authService = new AuthService();
