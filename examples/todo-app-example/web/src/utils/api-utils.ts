/* eslint-disable */
import axios, {AxiosRequestConfig, AxiosResponse} from 'axios';

export interface Response<T> {
    code: number;
    msg: string;
    errorResponse: string | null;
    responseHeaders: any;
    response: T;
}

export async function sendRequestJson<T>(
    data: any,
    url: string,
    method: 'GET' | 'POST' | 'PUT' | 'DELETE', // add more if needed
    requestHeader?: Record<string, string>
): Promise<Response<T>> {
    // Build default headers and merge with any passed in headers
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...requestHeader,
    };

    // Prepare and log request details
    let logMessage = `\nURL: ${url}\n- Request header:`;
    Object.entries(headers).forEach(([key, value]) => {
        logMessage += `\n    - ${key}: ${value}`;
    });

    let requestData: any = undefined;
    if (data && (method === 'POST' || method === 'PUT')) {
        requestData = data;
        const dataStr = JSON.stringify(data);
        logMessage += `\n- Request body: \n\t${dataStr}`;
    }

    const config: AxiosRequestConfig = {
        method: method.toLowerCase() as AxiosRequestConfig['method'],
        url,
        headers,
        data: requestData,
        // allow all status codes to pass through so we can handle errors manually
        validateStatus: () => true,
    };

    try {
        const response: AxiosResponse<T> = await axios.request<T>(config);

        logMessage += `\n- Response status code: ${response.status}`;
        logMessage += `\n- Response header:`;
        Object.entries(response.headers).forEach(([key, value]) => {
            logMessage += `\n    - ${key}: ${value}`;
        });
        // Ensure response body is a string for logging purposes
        const responseBody =
            typeof response.data === 'string'
                ? response.data
                : JSON.stringify(response.data);
        logMessage += `\n- Response body: \n\t${responseBody}`;

        // console.log(logMessage);

        return {
            code: response.status,
            msg: response.statusText,
            errorResponse: response.status === 200 ? null : responseBody,
            responseHeaders: response.headers,
            response: response.data,
        };
    } catch (error: any) {
        console.error('Request failed: ', error);
        throw error;
    }
}
