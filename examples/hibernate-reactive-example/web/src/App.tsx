import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/context/AuthContext";
import { Toaster } from "@/components/ui/sonner";
import { ThemeProvider } from "@/components/theme-provider";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import TodoListPage from "./pages/TodoListPage";
import { ProtectedRoute } from "./components/ProtectedRoute";

import { GlobalErrorDialog } from "./components/global-error-dialog";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ThemeProvider
          defaultTheme="dark"
          storageKey="vite-ui-theme"
          attribute="class"
        >
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/" element={<TodoListPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
          <Toaster />
          <GlobalErrorDialog />
        </ThemeProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
