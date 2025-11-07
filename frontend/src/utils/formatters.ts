import { format } from "date-fns";

export const formatCurrency = (
  amount: number,
  currency: string = "USD",
): string => {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
  }).format(amount);
};

export const formatDate = (
  date: string | Date,
  formatString: string = "PPP",
): string => {
  const dateObj = typeof date === "string" ? new Date(date) : date;
  return format(dateObj, formatString);
};

export const formatDateTime = (date: string | Date): string => {
  return formatDate(date, "PPP p");
};

export const truncateString = (str: string, maxLength: number): string => {
  if (str.length <= maxLength) return str;
  return str.slice(0, maxLength - 3) + "...";
};

export const convertIdWithPrifx = (prefix: string, id: number): string => {
  return `${prefix}-${id}`;
};

export const removePrfixToGetId = (id: string): string => {
  const [, idString] = id.split("-");

  return idString;
};
