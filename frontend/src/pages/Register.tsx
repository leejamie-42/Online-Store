import React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link } from "react-router-dom";
import { LuShoppingBag } from "react-icons/lu";
import { registerSchema, type RegisterFormData } from "@/schemas/auth.schema";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { PasswordStrength } from "@/components/auth/PasswordStrength";
import { useRegisterMutation } from "@/hooks/useAuthMutations";

export const Register: React.FC = () => {
  const registerMutation = useRegisterMutation();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const password = watch("password", "");

  const onSubmit = (data: RegisterFormData) => {
    const { username, email, password } = data;
    registerMutation.mutate({ name: username, email, password });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md">
        {/* Card Container */}
        <div className="bg-white rounded-2xl shadow-sm p-6">
          {/* Logo Icon */}
          <div className="flex justify-center mb-6">
            <div className="w-20 h-20 bg-primary-600 rounded-full flex items-center justify-center">
              <LuShoppingBag className="h-8 w-8 text-white" />
            </div>
          </div>

          {/* Header */}
          <div className="text-center mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Create Account</h2>
            <p className="text-sm text-gray-600">
              Join us and start shopping today
            </p>
          </div>

          {/* Form */}
          <form className="space-y-10" onSubmit={handleSubmit(onSubmit)}>
            {registerMutation.isError && (
              <div className="rounded-md bg-red-50 p-3">
                <p className="text-sm text-red-800">
                  {registerMutation.error?.message ||
                    "Registration failed. Please try again."}
                </p>
              </div>
            )}

            <div className="space-y-5">
              <Input
                label="Username"
                type="text"
                placeholder="Choose a username"
                autoComplete="username"
                variant="filled"
                {...register("username")}
                error={errors.username?.message}
              />

              <Input
                label="Email Address"
                type="email"
                placeholder="Enter your email"
                autoComplete="email"
                variant="filled"
                {...register("email")}
                error={errors.email?.message}
              />

              <div>
                <Input
                  label="Password"
                  type="password"
                  placeholder="Create a password"
                  autoComplete="new-password"
                  variant="filled"
                  {...register("password")}
                  error={errors.password?.message}
                />
                <PasswordStrength password={password} />
              </div>

              <Input
                label="Confirm Password"
                type="password"
                placeholder="Confirm your password"
                autoComplete="new-password"
                variant="filled"
                {...register("confirmPassword")}
                error={errors.confirmPassword?.message}
              />
            </div>

            <Button
              type="submit"
              variant="dark"
              fullWidth
              isLoading={registerMutation.isPending}
            >
              Create Account
            </Button>
          </form>

          <div className="text-center text-sm text-gray-600 pt-2">
            Already have an account?{" "}
            <Link to="/login" className="text-blue-600 hover:underline">
              Sign in
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
