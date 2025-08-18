import {App as AntDesignApp} from "antd";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import NotFound from "./utils/NotFound";
import Home from "./components/home/Home.tsx";
import {AppProvider} from "./context/AppProvider.tsx";
import Login from "./components/login/Login.tsx";
import SignUp from "./components/signup/SignUp.tsx";

function App() {
    return (
        <AntDesignApp>
            <AppProvider>
                <BrowserRouter>
                    <Routes>
                        <Route path="/" element={<Home/>}></Route>
                        <Route path="/login" element={<Login/>}></Route>
                        <Route path="/signup" element={<SignUp/>}></Route>
                        <Route path="*" element={<NotFound/>}/>
                    </Routes>
                </BrowserRouter>
            </AppProvider>
        </AntDesignApp>
    );
}

export default App;
