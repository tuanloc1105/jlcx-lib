import React, {useState} from "react";
import {useAppContext} from "../../context/AppProvider.tsx";
import {Button, ConfigProvider, Form, Input, notification, theme} from "antd";
import {useNavigateTo} from "../../utils/navigation.ts";
import {LoginOutlined} from "@ant-design/icons";
import {SignUpRequest} from "../../dto/ApiRequest.ts";
import {sendRequestJson} from "../../utils/api-utils.ts";
import {SignUpResponse} from "../../dto/ApiResponse.ts";

const SignUp: React.FC = () => {
    const navigateTo = useNavigateTo();
    const {lightTheme} = useAppContext();
    const [api, contextHolder] = notification.useNotification();
    type NotificationType = 'success' | 'info' | 'warning' | 'error';
    const [form] = Form.useForm();
    const [usernameInput, setUsernameInput] = useState("");
    const [passwordInput, setPasswordInput] = useState("");
    const [fullNameInput, setFullNameInput] = useState("");

    const openNotificationWithIcon = (type: NotificationType, contentL: string) => {
        api[type]({
            message: (
                <p className="font-bold font-sans">Notification</p>
            ),
            description: (
                <p className="font-sans">{contentL}</p>
            ),
        });
    };

    const handleSignUp = async (): Promise<void> => {
        const requestDate: SignUpRequest = {
            username: usernameInput,
            password: passwordInput,
            fullName: fullNameInput,
        }
        const signUpResult = await sendRequestJson<SignUpResponse>(
            requestDate,
            `${import.meta.env.VITE_BACKEND_API_URL}/user/create_new`,
            "POST",
            {}
        );
        if (signUpResult.code === 200) {
            navigateTo("/login");
        } else {
            const apiResponseMessage: string = signUpResult.response.errorDescription ? signUpResult.response.errorDescription : "Cannot proceed your sign up request";
            openNotificationWithIcon("error", apiResponseMessage);
            return;
        }
    }

    return (
        <>
            {contextHolder}
            <div
                className={
                    (
                        lightTheme ? "bg-gradient-to-r from-purple-900 to-yellow-50" :
                            "bg-gradient-to-r from-purple-900 to-stone-900"
                    ) + " w-screen h-screen flex flex-col items-center justify-center gap-5 font-sans"
                }
            >
                <ConfigProvider
                    theme={{
                        token: {
                            fontFamily: "Montserrat"
                        },
                        algorithm: lightTheme ? theme.defaultAlgorithm : theme.darkAlgorithm
                    }}
                >
                    <h1 className="bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent text-3xl font-bold">
                        TODO LIST
                    </h1>
                    <h1 className="bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent text-3xl font-bold">
                        SIGN UP
                    </h1>
                    <div
                        className="flex flex-col gap-2 w-1/2 items-center bg-white p-6 rounded-2xl shadow-2xl"
                    >
                        <Form
                            form={form}
                            variant="filled"
                            style={{
                                width: "100%",
                            }}
                            initialValues={{variant: 'filled'}}
                        >
                            <Form.Item label="Username" name="usernameInput"
                                       rules={[{required: true, message: 'Please enter username!'}]}>
                                <Input value={usernameInput} onChange={(e) => setUsernameInput(e.target.value)}/>
                            </Form.Item>
                            <Form.Item label="Password" name="passwordInput"
                                       rules={[{required: true, message: 'Please enter password!'}]}>
                                <Input value={passwordInput} onChange={(e) => setPasswordInput(e.target.value)}/>
                            </Form.Item>
                            <Form.Item label="Full name" name="fullNameInput"
                                       rules={[{required: true, message: 'Please enter your full name!'}]}>
                                <Input value={fullNameInput} onChange={(e) => setFullNameInput(e.target.value)}/>
                            </Form.Item>
                            <div
                                className="w-full flex justify-end"
                            >
                                <ConfigProvider
                                    theme={{
                                        token: {
                                            colorPrimary: "#8b5cf6",
                                            colorTextLightSolid: "black",
                                        }
                                    }}
                                >
                                    <Button
                                        type="primary"
                                        icon={<LoginOutlined/>}
                                        iconPosition="end"
                                        onClick={handleSignUp}
                                        disabled={!usernameInput || !passwordInput || !fullNameInput}
                                    >
                                        Sign up
                                    </Button>
                                </ConfigProvider>
                            </div>
                        </Form>
                    </div>
                </ConfigProvider>
            </div>
        </>
    );
}

export default SignUp;
