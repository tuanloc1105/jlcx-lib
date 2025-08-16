export interface LoginRequest {
    username: string;
    password: string;
}

export interface SignUpRequest {
    username: string;
    password: string;
    fullName: string;
}

export interface CreateTaskRequest {
    taskName: string;
    taskDetail: string;
    remindAt: string;
}

export interface ListAllTasksRequest {
    pageNumber: number;
}

export interface DeleteTasksRequest {
    id: number;
}

export interface MarkTaskAsDoneRequest {
    id: number;
}

export interface SearchTaskRequest {
    searchContent: string;
    pageNumber: number;
}
