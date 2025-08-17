import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Admin from './pages/Admin';
import AdminSetup from './pages/AdminSetup';
import Profile from './pages/Profile';
import Transactions from './pages/Transactions';
import Reports from './pages/Reports';
import AuditDashboard from './pages/AuditDashboard';

const App: React.FC = () => {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/admin-setup" element={<AdminSetup />} />
            
            {/* Protected routes */}
            <Route path="/" element={
              <ProtectedRoute>
                <div>
                  <Navbar />
                  <Dashboard />
                </div>
              </ProtectedRoute>
            } />
            
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <div>
                  <Navbar />
                  <Dashboard />
                </div>
              </ProtectedRoute>
            } />
            
            <Route path="/transactions" element={
              <ProtectedRoute>
                <div>
                  <Navbar />
                  <Transactions />
                </div>
              </ProtectedRoute>
            } />
            
            <Route path="/reports" element={
              <ProtectedRoute>
                <div>
                  <Navbar />
                  <Reports />
                </div>
              </ProtectedRoute>
            } />
            
            <Route path="/profile" element={
              <ProtectedRoute>
                <div>
                  <Navbar />
                  <Profile />
                </div>
              </ProtectedRoute>
            } />
            
            <Route path="/admin" element={
              <ProtectedRoute requireAdmin>
                <div>
                  <Navbar />
                  <Admin />
                </div>
              </ProtectedRoute>
            } />
            
            <Route path="/audit" element={
              <ProtectedRoute requireAdmin>
                <div>
                  <Navbar />
                  <AuditDashboard />
                </div>
              </ProtectedRoute>
            } />
                         
                         {/* Redirect root to dashboard */}
                         <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
};

export default App;
