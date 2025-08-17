import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8083';

const bankingApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
bankingApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface Account {
  id: number;
  accountNumber: string;
  balance: number;
  accountType: string;
  status: string;
  clientId: number;
  createdAt: string;
  updatedAt: string;
}

export interface Transaction {
  id: number;
  transactionType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';
  amount: number;
  balanceAfter: number;
  description: string;
  accountId: number;
  referenceNumber: string;
  createdAt: string;
}

export interface TransactionRequest {
  accountId: number;
  transactionType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';
  amount: number;
  description?: string;
  destinationAccountId?: number; // For transfers
}

export interface TransactionResponse {
  id: number;
  transactionType: string;
  amount: number;
  balanceAfter: number;
  description: string;
  accountId: number;
  referenceNumber: string;
  createdAt: string;
}

class BankingService {
  // Account Management
  async getAccountsByClient(clientId: number): Promise<Account[]> {
    const response = await bankingApi.get<Account[]>(`/api/v1/accounts/client/${clientId}`);
    return response.data;
  }

  async getAccountBalance(accountId: number): Promise<number> {
    const response = await bankingApi.get<number>(`/api/v1/accounts/${accountId}/balance`);
    return response.data;
  }

  async createAccount(clientId: number, accountType: string): Promise<Account> {
    const response = await bankingApi.post<Account>(`/api/v1/accounts?clientId=${clientId}&accountType=${accountType}`);
    return response.data;
  }

  // Transaction Management
  async processDeposit(request: TransactionRequest): Promise<TransactionResponse> {
    const response = await bankingApi.post<TransactionResponse>('/api/v1/transactions/deposit', request);
    return response.data;
  }

  async processWithdrawal(request: TransactionRequest): Promise<TransactionResponse> {
    const response = await bankingApi.post<TransactionResponse>('/api/v1/transactions/withdraw', request);
    return response.data;
  }

  async processTransfer(request: TransactionRequest): Promise<TransactionResponse> {
    const response = await bankingApi.post<TransactionResponse>('/api/v1/transactions/transfer', request);
    return response.data;
  }

  async getTransactionsByAccount(accountId: number): Promise<Transaction[]> {
    const response = await bankingApi.get<Transaction[]>(`/api/v1/transactions/account/${accountId}`);
    return response.data;
  }

  async getTransactionsByClient(clientId: number): Promise<Transaction[]> {
    const response = await bankingApi.get<Transaction[]>(`/api/v1/transactions/client/${clientId}`);
    return response.data;
  }

  async getTransactionById(transactionId: number): Promise<TransactionResponse> {
    const response = await bankingApi.get<TransactionResponse>(`/api/v1/transactions/${transactionId}`);
    return response.data;
  }

  // Check if account can transact
  async canAccountTransact(accountId: number): Promise<boolean> {
    const response = await bankingApi.get<boolean>(`/api/v1/accounts/${accountId}/can-transact`);
    return response.data;
  }
}

export const bankingService = new BankingService();
