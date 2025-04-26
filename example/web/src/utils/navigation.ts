import { useNavigate } from "react-router-dom";

// Custom hook để sử dụng điều hướng trong bất kỳ component nào
export const useNavigateTo = () => {
    const navigate = useNavigate();
    return (path: string, options?: { replace?: boolean }) => {
        navigate(path, options);
    };
};
