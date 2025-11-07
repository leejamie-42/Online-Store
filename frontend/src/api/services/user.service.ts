import { apiClient } from '@/lib/axios';
import { API_ENDPOINTS } from '@/config/api.config';
import type { User, Address } from '@/types/user.types';

export const userService = {
  async getProfile(): Promise<User> {
    const response = await apiClient.get<User>(API_ENDPOINTS.USER_PROFILE);
    return response.data;
  },

  async updateProfile(data: Partial<User>): Promise<User> {
    const response = await apiClient.put<User>(API_ENDPOINTS.USER_PROFILE, data);
    return response.data;
  },

  async getAddresses(): Promise<Address[]> {
    const response = await apiClient.get<Address[]>(API_ENDPOINTS.USER_ADDRESSES);
    return response.data;
  },

  async addAddress(address: Address): Promise<{ addressId: string; message: string }> {
    const response = await apiClient.post(API_ENDPOINTS.USER_ADDRESSES, address);
    return response.data;
  },

  async updateAddress(addressId: string, address: Address): Promise<void> {
    await apiClient.put(API_ENDPOINTS.USER_ADDRESS(addressId), address);
  },

  async deleteAddress(addressId: string): Promise<void> {
    await apiClient.delete(API_ENDPOINTS.USER_ADDRESS(addressId));
  },
};
