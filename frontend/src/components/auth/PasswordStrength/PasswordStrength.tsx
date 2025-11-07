import React from "react";

interface PasswordStrengthProps {
  password: string;
}

interface StrengthResult {
  score: number;
  label: string;
  color: string;
  bgColor: string;
}

const calculatePasswordStrength = (password: string): StrengthResult => {
  if (!password) {
    return { score: 0, label: "", color: "", bgColor: "" };
  }

  let score = 0;

  // Length check
  if (password.length >= 8) score += 1;
  if (password.length >= 12) score += 1;

  // Contains lowercase
  if (/[a-z]/.test(password)) score += 1;

  // Contains uppercase
  if (/[A-Z]/.test(password)) score += 1;

  // Contains numbers
  if (/[0-9]/.test(password)) score += 1;

  // Contains special characters
  if (/[^A-Za-z0-9]/.test(password)) score += 1;

  // Map score to strength level
  if (score <= 2) {
    return {
      score: 1,
      label: "Weak",
      color: "text-red-600",
      bgColor: "bg-red-500",
    };
  } else if (score <= 4) {
    return {
      score: 2,
      label: "Fair",
      color: "text-yellow-600",
      bgColor: "bg-yellow-500",
    };
  } else if (score <= 5) {
    return {
      score: 3,
      label: "Good",
      color: "text-blue-600",
      bgColor: "bg-blue-500",
    };
  } else {
    return {
      score: 4,
      label: "Strong",
      color: "text-green-600",
      bgColor: "bg-green-500",
    };
  }
};

export const PasswordStrength: React.FC<PasswordStrengthProps> = ({
  password,
}) => {
  const strength = calculatePasswordStrength(password);

  if (!password) {
    return null;
  }

  return (
    <div className="mt-2">
      <div className="flex items-center justify-between mb-1">
        <span className="text-sm text-gray-600">Password strength:</span>
        <span className={`text-sm font-medium ${strength.color}`}>
          {strength.label}
        </span>
      </div>
      <div className="flex gap-1">
        {[1, 2, 3, 4].map((level) => (
          <div
            key={level}
            className={`h-2 flex-1 rounded-full transition-colors ${
              level <= strength.score ? strength.bgColor : "bg-gray-200"
            }`}
          />
        ))}
      </div>
      <div className="mt-2 text-xs text-gray-500">
        <ul className="list-disc list-inside space-y-1">
          <li className={password.length >= 8 ? "text-green-600" : ""}>
            At least 8 characters
          </li>
          <li className={/[A-Z]/.test(password) ? "text-green-600" : ""}>
            One uppercase letter
          </li>

          {/* 
          <li className={/[a-z]/.test(password) ? "text-green-600" : ""}>
            One lowercase letter
          </li>
          */}

          <li className={/[0-9]/.test(password) ? "text-green-600" : ""}>
            One number
          </li>
        </ul>
      </div>
    </div>
  );
};
