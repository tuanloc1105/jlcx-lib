export interface ApiResponse {
    errorCode: number;
    errorDescription: string;
    trace: string;
}

export interface PageResponse<T> {
    pageNumber: number;
    pageSize: number;
    totalPages: number;
    numberOfElements: number;
    totalElements: number;
    firstPage: boolean;
    lastPage: boolean;
    content: T[];
}

export interface LoginResponse extends ApiResponse {
    fullName: string;
    token: string;
}

export interface SignUpResponse extends ApiResponse {
    username: string;
    full_name: string;
    user_uid: string;
}

export interface ListAllTasksResponse extends ApiResponse {
    tasks: PageResponse<TaskItem>;
}

export interface TaskItem {
    id: number;
    taskName: string;
    taskDetail: string;
    remindAt: string;
    createdTime: string;
    updatedTime: string;
    finished: boolean;
    createdBy: string;
    updatedBy: string;
}
