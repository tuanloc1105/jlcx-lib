import React, {createContext, ReactNode, useContext, useState} from "react";

interface AppContextType {
    lightTheme: boolean;
    setLightTheme: React.Dispatch<React.SetStateAction<boolean>>;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

interface AppProviderProps {
    children: ReactNode;
}

export const AppProvider: React.FC<AppProviderProps> = ({ children }) => {
    const [lightTheme, setLightTheme] = useState(true);

    return (
        <AppContext.Provider value={{ lightTheme, setLightTheme }}>
            {children}
        </AppContext.Provider>
    );
};

export const useAppContext = (): AppContextType => {
    const context = useContext(AppContext);
    if (!context) {
        throw new Error("useAppContext must be used within an AppProvider");
    }
    return context;
};
