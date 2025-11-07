import React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link } from "react-router-dom";
import { LuShoppingBag } from "react-icons/lu";
import { loginSchema, type LoginFormData } from "@/schemas/auth.schema";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { useLoginMutation } from "@/hooks/useAuthMutations";

export const Login: React.FC = () => {
  const loginMutation = useLoginMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = (data: LoginFormData) => {
    loginMutation.mutate({
      username: data.username,
      password: data.password,
    });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md">
        {/* Card Container */}
        <div className="bg-white rounded-2xl shadow-sm p-8">
          {/* Logo Icon */}
          <div className="flex justify-center mb-6">
            <div className="w-20 h-20 bg-primary-600 rounded-full flex items-center justify-center">
              <LuShoppingBag className="h-10 w-10 text-white" />
            </div>
          </div>

          {/* Header */}
          <div className="text-center mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Welcome Back
            </h2>
            <p className="text-sm text-gray-600">
              Sign in to your account to continue shopping
            </p>
          </div>

          {/* Form */}
          <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
            {loginMutation.isError && (
              <div className="rounded-md bg-red-50 p-3">
                <p className="text-sm text-red-800">
                  {loginMutation.error?.message ||
                    "Login failed. Please check your credentials."}
                </p>
              </div>
            )}

            <Input
              label="Username"
              placeholder="Enter your username"
              autoComplete="username"
              variant="filled"
              {...register("username")}
              error={errors.username?.message}
            />

            <Input
              label="Password"
              type="password"
              placeholder="Enter your password"
              autoComplete="current-password"
              variant="filled"
              {...register("password")}
              error={errors.password?.message}
            />

            <div className="flex items-center">
              <input
                id="remember-me"
                name="remember-me"
                type="checkbox"
                className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label
                htmlFor="remember-me"
                className="ml-2 block text-sm text-gray-700"
              >
                Remember me
              </label>
            </div>

            <Button
              type="submit"
              variant="dark"
              fullWidth
              isLoading={loginMutation.isPending}
            >
              Sign In
            </Button>

            <div className="text-center text-sm text-gray-600">
              Don't have an account?{" "}
              <Link to="/register" className="text-blue-600 hover:underline">
                Sign up
              </Link>
            </div>

            {/* Demo Credentials */}
            <p className="text-center text-sm text-gray-500 pt-4 border-t">
              Demo credentials: customer / COMP5348
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};
