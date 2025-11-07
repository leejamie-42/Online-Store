import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { QueryProvider } from "@/providers/QueryProvider";
import { AuthProvider } from "@/context/AuthContext";
import { MainLayout } from "@/components/layout";
import { ProtectedRoute } from "@/components/common/ProtectedRoute";
import { ROUTES } from "@/config/routes";
import Home from "./pages/Home";
import About from "./pages/About";
import Contact from "./pages/Contact";
import NotFound from "./pages/NotFound";
import ProductDetail from "./pages/ProductDetail";
import { Login } from "./pages/Login";
import { Register } from "./pages/Register";
import { ForgotPassword } from "./pages/ForgotPassword";
import OrderTracking from "./pages/OrderTracking";
import OrderDetail from "./pages/OrderDetail";
import OrderHistory from "./pages/OrderHistory";
import {
  CheckoutReview,
  CheckoutDetails,
  BpayPaymentPage,
  CheckoutConfirmationPage,
} from "./pages/checkout";

function App() {
  return (
    <QueryProvider>
      <AuthProvider>
        <Router>
          <Routes>
            {/* Authentication routes - without MainLayout */}
            <Route path={ROUTES.LOGIN} element={<Login />} />
            <Route path={ROUTES.REGISTER} element={<Register />} />
            <Route path={ROUTES.FORGOT_PASSWORD} element={<ForgotPassword />} />

            {/* Public routes - with MainLayout */}
            <Route
              path="/"
              element={
                <MainLayout>
                  <Home />
                </MainLayout>
              }
            />
            <Route
              path="/products/:id"
              element={
                <MainLayout>
                  <ProductDetail />
                </MainLayout>
              }
            />
            <Route
              path="/about"
              element={
                <MainLayout>
                  <About />
                </MainLayout>
              }
            />
            <Route
              path="/contact"
              element={
                <MainLayout>
                  <Contact />
                </MainLayout>
              }
            />

            {/* Protected routes - require authentication */}
            <Route
              path={ROUTES.CART}
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <div className="max-w-7xl mx-auto px-4 py-8">
                      <h1 className="text-2xl font-bold">Shopping Cart</h1>
                      <p className="text-gray-600 mt-4">
                        Cart functionality coming soon...
                      </p>
                    </div>
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            {/* Checkout routes - nested multi-step flow */}
            <Route
              path="/checkout"
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <Navigate to={ROUTES.CHECKOUT_REVIEW} replace />
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.CHECKOUT_REVIEW}
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <CheckoutReview />
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.CHECKOUT_DETAILS}
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <CheckoutDetails />
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            {/* Payment route with payment ID */}
            <Route
              path="/checkout/payment/:paymentId"
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <BpayPaymentPage />
                  </MainLayout>
                </ProtectedRoute>
              }
            />

            <Route
              path={ROUTES.CHECKOUT_CONFIRMATION(":orderId")}
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <CheckoutConfirmationPage />
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.ORDERS}
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <OrderHistory />
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/orders/:orderId"
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <OrderDetail />
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/orders/:orderId/tracking"
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <OrderTracking />
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.PROFILE}
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <div className="max-w-7xl mx-auto px-4 py-8">
                      <h1 className="text-2xl font-bold">My Profile</h1>
                      <p className="text-gray-600 mt-4">
                        Profile functionality coming soon...
                      </p>
                    </div>
                  </MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.SETTINGS}
              element={
                <ProtectedRoute>
                  <MainLayout>
                    <div className="max-w-7xl mx-auto px-4 py-8">
                      <h1 className="text-2xl font-bold">Settings</h1>
                      <p className="text-gray-600 mt-4">
                        Settings functionality coming soon...
                      </p>
                    </div>
                  </MainLayout>
                </ProtectedRoute>
              }
            />

            {/* 404 - with MainLayout */}
            <Route
              path="*"
              element={
                <MainLayout>
                  <NotFound />
                </MainLayout>
              }
            />
          </Routes>
        </Router>
      </AuthProvider>
    </QueryProvider>
  );
}

export default App;
