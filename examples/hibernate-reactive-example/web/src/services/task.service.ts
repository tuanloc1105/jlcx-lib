import apiClient from "./api.client";

export interface Task {
  id: number;
  taskTitle: string;
  taskDetail: string;
  finished: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  taskTitle: string;
  taskDetail: string;
}

export interface UpdateTaskRequest {
  id: number;
  taskTitle: string;
  taskDetail: string;
  finished: boolean;
}

export interface TaskListResponse {
  data: {
    content: Task[];
    pageNumber: number;
    pageSize: number;
    totalPages: number;
    totalElements: number;
    firstPage: boolean;
    lastPage: boolean;
    numberOfElements: number;
  };
  errorCode: number;
  errorDescription: string;
  httpCode: number;
}

export interface TaskDetailResponse {
  data: Task;
  errorCode: number;
  errorDescription: string;
  httpCode: number;
}

export interface GenericResponse {
  errorCode: number;
  errorDescription: string;
  httpCode: number;
  trace?: string;
}

export const taskService = {
  createTask: (data: CreateTaskRequest): Promise<GenericResponse> =>
    apiClient.post("/api/tasks/create_task", data),
  getAllTasks: (pageNumber: number): Promise<TaskListResponse> =>
    apiClient.post("/api/tasks/get_all_task", { pageNumber }),
  getTaskDetail: (id: number): Promise<TaskDetailResponse> =>
    apiClient.post(`/api/tasks/get_task_detail`, { id }),
  searchTask: (
    pageNumber: number,
    searchContent: string
  ): Promise<TaskListResponse> =>
    apiClient.post("/api/tasks/search_task_by_name", {
      pageNumber,
      searchContent,
    }),
  updateTask: (data: UpdateTaskRequest): Promise<GenericResponse> =>
    apiClient.post("/api/tasks/update_task", data),
  deleteTask: (id: number): Promise<GenericResponse> =>
    apiClient.post("/api/tasks/delete_task", { id }),
};
