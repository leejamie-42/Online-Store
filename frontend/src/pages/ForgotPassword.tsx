import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link } from "react-router-dom";
import { LuArrowLeft, LuShoppingCart, LuMail } from "react-icons/lu";
import {
  forgotPasswordSchema,
  type ForgotPasswordFormData,
} from "@/schemas/auth.schema";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { ROUTES } from "@/config/routes";

export const ForgotPassword: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [serverError, setServerError] = useState<string>("");

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormData>({
    resolver: zodResolver(forgotPasswordSchema),
  });

  const onSubmit = async (data: ForgotPasswordFormData) => {
    try {
      setIsLoading(true);
      setServerError("");

      // TODO: Implement actual password reset API call
      // await authService.resetPassword(data.email);
      console.log("Password reset requested for:", data.email);
      await new Promise((resolve) => setTimeout(resolve, 2000));

      setIsSuccess(true);
    } catch (error) {
      setServerError(
        error instanceof Error
          ? error.message
          : "Failed to send reset email. Please try again.",
      );
    } finally {
      setIsLoading(false);
    }
  };

  if (isSuccess) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          <div>
            <Link
              to={ROUTES.HOME}
              className="flex justify-center items-center space-x-2"
            >
              <LuShoppingCart className="h-12 w-12 text-primary-600" />
              <span className="text-3xl font-bold text-gray-900">Store</span>
            </Link>
            <div className="mt-6 flex justify-center">
              <div className="rounded-full bg-green-100 p-3">
                <LuMail className="h-12 w-12 text-green-600" />
              </div>
            </div>
            <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
              Check your email
            </h2>
            <p className="mt-2 text-center text-sm text-gray-600">
              We've sent a password reset link to your email address. Please
              check your inbox and follow the instructions to reset your
              password.
            </p>
          </div>

          <div className="mt-8 space-y-4">
            <Link to={ROUTES.LOGIN}>
              <Button variant="primary" fullWidth>
                Back to Sign in
              </Button>
            </Link>
            <p className="text-center text-sm text-gray-600">
              Didn't receive the email?{" "}
              <button
                onClick={() => setIsSuccess(false)}
                className="font-medium text-primary-600 hover:text-primary-500"
              >
                Try again
              </button>
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {/* Header */}
        <div>
          <Link
            to={ROUTES.HOME}
            className="flex justify-center items-center space-x-2"
          >
            <LuShoppingCart className="h-12 w-12 text-primary-600" />
            <span className="text-3xl font-bold text-gray-900">Store</span>
          </Link>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Reset your password
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Enter your email address and we'll send you a link to reset your
            password.
          </p>
        </div>

        {/* Form */}
        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          {serverError && (
            <div className="rounded-md bg-red-50 p-4">
              <p className="text-sm text-red-800">{serverError}</p>
            </div>
          )}

          <Input
            label="Email address"
            type="email"
            autoComplete="email"
            {...register("email")}
            error={errors.email?.message}
          />

          <Button
            type="submit"
            variant="primary"
            fullWidth
            isLoading={isLoading}
          >
            Send reset link
          </Button>

          <div className="text-center">
            <Link
              to={ROUTES.LOGIN}
              className="flex items-center justify-center text-sm font-medium text-primary-600 hover:text-primary-500"
            >
              <LuArrowLeft className="h-4 w-4 mr-1" />
              Back to Sign in
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
};
