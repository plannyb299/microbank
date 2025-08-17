import { AxiosError } from 'axios';

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  errorCode?: string;
  message: string;
  details?: string;
}

/**
 * Extract user-friendly error message from API response
 */
export function extractErrorMessage(error: any): string {
  if (error instanceof AxiosError) {
    const response = error.response;
    
    if (response?.data) {
      // Check if it's our custom error response format
      if (response.data.message && response.data.errorCode) {
        return response.data.message;
      }
      
      // Fallback to generic error message
      if (response.data.message) {
        return response.data.message;
      }
      
      // Handle different HTTP status codes
      switch (response.status) {
        case 401:
          return 'Authentication required. Please log in again.';
        case 403:
          return 'Access denied. You do not have permission to perform this action.';
        case 404:
          return 'The requested resource was not found.';
        case 500:
          return 'An internal server error occurred. Please try again later.';
        default:
          return 'An error occurred while processing your request.';
      }
    }
  }
  
  // Fallback for non-Axios errors
  if (error.message) {
    return error.message;
  }
  
  return 'An unexpected error occurred. Please try again later.';
}

/**
 * Extract error code from API response
 */
export function extractErrorCode(error: any): string | undefined {
  if (error instanceof AxiosError && error.response?.data?.errorCode) {
    return error.response.data.errorCode;
  }
  return undefined;
}

/**
 * Check if error is a client access issue (blacklisted, inactive, etc.)
 */
export function isClientAccessError(error: any): boolean {
  const errorCode = extractErrorCode(error);
  return errorCode === 'CLIENT_BLACKLISTED' || 
         errorCode === 'CLIENT_INACTIVE' || 
         errorCode === 'CLIENT_TRANSACTION_BLOCKED' ||
         errorCode === 'CLIENT_NOT_FOUND' ||
         errorCode === 'UNAUTHORIZED_ACCESS' ||
         errorCode === 'SERVICE_UNAVAILABLE';
}

/**
 * Get specific error message based on error code
 */
export function getSpecificErrorMessage(error: any): string {
  const errorCode = extractErrorCode(error);
  
  switch (errorCode) {
    case 'CLIENT_BLACKLISTED':
      return 'Your account has been temporarily suspended. Please contact customer support for assistance.';
    case 'CLIENT_INACTIVE':
      return 'Your account is currently inactive. Please contact customer support to reactivate your account.';
    case 'CLIENT_TRANSACTION_BLOCKED':
      return 'You are not authorized to perform this transaction. Please contact customer support for assistance.';
    case 'CLIENT_NOT_FOUND':
      return 'Client account not found. Please contact customer support for assistance.';
    case 'UNAUTHORIZED_ACCESS':
      return 'You are not authorized to access this service. Please contact customer support for assistance.';
    case 'SERVICE_UNAVAILABLE':
      return 'Service is temporarily unavailable. Please try again later or contact customer support.';
    default:
      return extractErrorMessage(error);
  }
}
