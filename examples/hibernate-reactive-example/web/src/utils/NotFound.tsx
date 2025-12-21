import React from "react";
import {useNavigateTo} from "./navigation.ts";

const NotFound: React.FC = () => {
    const navigateTo = useNavigateTo();

    return (
        <div className="flex flex-col justify-center items-center bg-gray-700 h-screen w-screen gap-14 font-sans">
            <h1 className="text-8xl text-red-600 animate-pulse" style={{animationDuration: '5s'}}>!!!404 🤔</h1>
            <h2 className="text-2xl">Oops! Có vẻ như trang mà bạn đang tìm không tồn tại.</h2>
            <h2 className="text-2xl transition-transform duration-300 ease-in-out hover:scale-105 active:scale-95">
                Quay trở về <span className="cursor-pointer text-blue-300"
                      onClick={() => {
                          navigateTo("/")
                      }}
                >
                    trang chủ
                </span>.
            </h2>
        </div>
    );
};

export default NotFound;
