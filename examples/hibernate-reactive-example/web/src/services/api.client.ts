import axios from "axios";

const apiClient = axios.create({
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

apiClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    // Handle 401 Unauthorized globally
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
      return Promise.reject(error);
    }

    let message = "An unexpected error occurred.";
    if (error.response?.data?.errorDescription) {
      message = error.response.data.errorDescription;
    } else if (error.message) {
      message = error.message;
    }

    // Dispatch custom event for GlobalErrorDialog
    window.dispatchEvent(
      new CustomEvent("api-error", {
        detail: { message },
      }),
    );

    return Promise.reject(error);
  },
);

export default apiClient;
