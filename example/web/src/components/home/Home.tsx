import React, {useEffect, useState} from "react";
import {useAppContext} from "../../context/AppProvider.tsx";
import {
    Button,
    ConfigProvider,
    DatePicker,
    FloatButton,
    GetProps,
    Input,
    Modal,
    notification,
    Table,
    TableProps,
    Tag,
    theme
} from 'antd';
import {LogoutOutlined, MoonOutlined, PlusOutlined, SunOutlined} from "@ant-design/icons";
import {useNavigateTo} from "../../utils/navigation.ts";
import Loading from "../common/Loading.tsx";
import {
    CreateTaskRequest,
    DeleteTasksRequest,
    ListAllTasksRequest,
    MarkTaskAsDoneRequest
} from "../../dto/ApiRequest.ts";
import {sendRequestJson} from "../../utils/api-utils.ts";
import {ApiResponse, ListAllTasksResponse, TaskItem} from "../../dto/ApiResponse.ts";
import {API_SUCCESS_CODE} from "../../utils/constant.ts";

// type RangePickerProps = GetProps<typeof DatePicker.RangePicker>;

const Home: React.FC = () => {
    // const {message} = App.useApp();
    const navigateTo = useNavigateTo();
    const [api, contextHolder] = notification.useNotification();
    type NotificationType = 'success' | 'info' | 'warning' | 'error';
    type SearchProps = GetProps<typeof Input.Search>;
    const {Search} = Input;
    const {lightTheme, setLightTheme} = useAppContext();
    const onSearch: SearchProps['onSearch'] = (value, _e, info) =>
        console.log(info?.source, value);
    const [loading, setLoading] = useState<boolean>(false);
    const [tasksListPageNumber, setTasksListPageNumber] = useState(1);
    const [totalTasksCount, setTotalTasksCount] = useState(0);
    const [tasksData, setTasksData] = useState<TaskItem[]>([]);
    const [isCreateTaskModalOpen, setIsCreateTaskModalOpen] = useState(false);
    const [isSignOutModalOpen, setIsSignOutModalOpen] = useState(false);
    const [newTaskDataTaskName, setNewTaskDataTaskName] = useState("");
    const [newTaskDataTaskDetail, setNewTaskDataTaskDetail] = useState("");
    const [newTaskDataRemindAt, setNewTaskDataRemindAt] = useState("");

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

    const showSignOutModal = () => {
        setIsSignOutModalOpen(true);
    }

    const handleSignOutOk = () => {
        setIsSignOutModalOpen(false);
    }

    const handleSignOutCancel = () => {
        setIsSignOutModalOpen(false);
    }

    const showTaskModal = () => {
        setIsCreateTaskModalOpen(true);
    };

    const handleCreateTaskOk = async () => {
        setLoading(true);
        try {
            const request: CreateTaskRequest = {
                taskName: newTaskDataTaskName,
                taskDetail: newTaskDataTaskDetail,
                remindAt: newTaskDataRemindAt
            }
            const createTaskResult = await sendRequestJson<ApiResponse>(
                request,
                `${import.meta.env.VITE_BACKEND_API_URL}/task/create_task`,
                "POST",
                {
                    "Authorization": `Bearer ${localStorage.getItem("access_token")}`
                }
            );
            if (createTaskResult.code === 401) {
                navigateTo("/");
            }
            const isSuccess: boolean = createTaskResult.code === 200 && createTaskResult.response.errorCode === API_SUCCESS_CODE;
            openNotificationWithIcon(
                isSuccess ? "success" : "warning",
                isSuccess ? "Remove task success" : createTaskResult.response.errorDescription
            );
        } catch (error) {
            console.log(error);
            openNotificationWithIcon("error", "An unexpected error occurred.");
        } finally {
            setLoading(false);
            setIsCreateTaskModalOpen(false);
        }
    };

    const handleCreateTaskCancel = () => {
        setIsCreateTaskModalOpen(false);
    };

    const handleGetTasks = async (pageNum: number | 0): Promise<void> => {
        setLoading(true);
        try {
            const request: ListAllTasksRequest = {
                pageNumber: pageNum === 0 ? tasksListPageNumber : pageNum
            }
            const listAllTasksResult = await sendRequestJson<ListAllTasksResponse>(
                request,
                `${import.meta.env.VITE_BACKEND_API_URL}/task/get_all_task`,
                "POST",
                {
                    "Authorization": `Bearer ${localStorage.getItem("access_token")}`
                }
            );
            if (listAllTasksResult.code === 401) {
                navigateTo("/");
            }
            setTotalTasksCount(listAllTasksResult.response.tasks.totalElements);
            if (listAllTasksResult.response.tasks.totalElements > 0) {
                setTasksData(listAllTasksResult.response.tasks.content);
            }
        } catch (error) {
            console.log(error);
            openNotificationWithIcon("error", "An unexpected error occurred.");
        } finally {
            setLoading(false);
        }
    };

    const handleRemoveTask = async (task: TaskItem) => {
        setLoading(true);
        try {
            const request: DeleteTasksRequest = {
                id: task.id
            }
            const removeTaskResult = await sendRequestJson<ApiResponse>(
                request,
                `${import.meta.env.VITE_BACKEND_API_URL}/task/delete_task`,
                "POST",
                {
                    "Authorization": `Bearer ${localStorage.getItem("access_token")}`
                }
            );
            if (removeTaskResult.code === 401) {
                navigateTo("/");
            }
            const isSuccess: boolean = removeTaskResult.code === 200 && removeTaskResult.response.errorCode === API_SUCCESS_CODE;
            openNotificationWithIcon(
                isSuccess ? "success" : "warning",
                isSuccess ? "Remove task success" : removeTaskResult.response.errorDescription
            );
        } catch (error) {
            console.log(error);
            openNotificationWithIcon("error", "An unexpected error occurred.");
        } finally {
            setLoading(false);
        }
    }

    const handleMarkTaskAsDone = async (task: TaskItem) => {
        setLoading(true);
        try {
            const request: MarkTaskAsDoneRequest = {
                id: task.id
            }
            const removeTaskResult = await sendRequestJson<ApiResponse>(
                request,
                `${import.meta.env.VITE_BACKEND_API_URL}/task/mark_task_as_finished`,
                "POST",
                {
                    "Authorization": `Bearer ${localStorage.getItem("access_token")}`
                }
            );
            if (removeTaskResult.code === 401) {
                navigateTo("/");
            }
            const isSuccess: boolean = removeTaskResult.code === 200 && removeTaskResult.response.errorCode === API_SUCCESS_CODE;
            openNotificationWithIcon(
                isSuccess ? "success" : "warning",
                isSuccess ? "Finished task" : removeTaskResult.response.errorDescription
            );
        } catch (error) {
            console.log(error);
            openNotificationWithIcon("error", "An unexpected error occurred.");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (!localStorage.getItem("access_token")) {
            openNotificationWithIcon("warning", "You have not logged in");
            navigateTo("/login");
        }
        handleGetTasks(0).then();
    }, []);

    const columns: TableProps<TaskItem>['columns'] = [
        {
            title: 'Task',
            dataIndex: "taskName",
            key: "taskName",
        },
        {
            title: 'Detail',
            dataIndex: "taskDetail",
            key: "taskDetail",
        },
        {
            title: 'Remind at',
            dataIndex: "remindAt",
            key: "remindAt",
        },
        {
            title: 'Last updated',
            dataIndex: "updatedTime",
            key: "updatedTime",
        },
        {
            title: 'Status',
            key: 'status',
            render: (_, record: TaskItem) => (
                <>
                    <div className="flex gap-2">
                        <Tag
                            color={record.finished ? "green" : "volcano"}
                        >
                            {record.finished ? "Finished" : "Waiting"}
                        </Tag>
                    </div>
                </>
            )
        },
        {
            title: 'Action',
            key: 'action',
            render: (_, record: TaskItem) => (
                <>
                    <div className="flex flex-col gap-2">
                        <Button
                            style={{
                                width: 'fit-content',
                                color: 'black',
                            }}
                            color="yellow"
                            variant="solid"
                            onClick={async () => {
                                await handleMarkTaskAsDone(record);
                                await handleGetTasks(tasksListPageNumber);
                            }}
                        >
                            Mark as done
                        </Button>
                        <Button
                            style={{
                                width: 'fit-content',
                                color: 'black',
                            }}
                            color="red"
                            variant="solid"
                            onClick={async () => {
                                await handleRemoveTask(record);
                                await handleGetTasks(tasksListPageNumber);
                            }}
                        >
                            Remove
                        </Button>
                    </div>
                </>
            )
        }
    ];

    return (
        <>
            {contextHolder}
            <div
                className={
                    (
                        lightTheme ? "bg-gradient-to-r from-purple-900 to-yellow-50" :
                            "bg-gradient-to-r from-purple-900 to-stone-900"
                    ) + " w-screen h-screen flex flex-col items-center justify-center gap-5 font-sans overflow-auto"
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

                    <FloatButton
                        onClick={() => showTaskModal()}
                        style={{insetInlineEnd: 24}}
                        icon={<PlusOutlined/>}
                    />

                    <FloatButton
                        onClick={() => showSignOutModal()}
                        style={{insetInlineEnd: 94}}
                        icon={<LogoutOutlined/>}
                    />

                    <h1 className="bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent text-3xl font-bold">
                        TODO LIST
                    </h1>
                    <div
                        style={{
                            maxHeight: "22px",
                            width: "70%",
                        }}
                        className="flex gap-2"
                    >
                        <ConfigProvider
                            theme={{
                                token: {
                                    colorPrimary: "#8b5cf6", // violet-500
                                }
                            }}
                        >
                            <Search
                                style={{
                                    height: "100%",
                                }}
                                placeholder="Enter your search to find task..."
                                onSearch={onSearch}
                                enterButton
                            />
                            <Button
                                type="primary"
                                icon={lightTheme ? <MoonOutlined/> : <SunOutlined/>}
                                onClick={() => {
                                    setLightTheme(!lightTheme);
                                }}
                            />
                        </ConfigProvider>
                    </div>
                    <div
                        style={{
                            width: "70%",
                        }}
                        className="overflow-auto"
                    >
                        <Table<TaskItem>
                            rowKey={'id'}
                            columns={columns}
                            dataSource={tasksData}
                            scroll={{x: 'max-content'}}
                            style={{maxWidth: '100%'}}
                            pagination={{
                                current: tasksListPageNumber,
                                total: totalTasksCount,
                                pageSize: 10,
                                showTotal: (total, range) => `${range[0]}-${range[1]} of ${total} element(s)`,
                                onChange: async (page, pageSize) => {
                                    console.log("reload - " + page + " - " + pageSize);
                                    setTasksListPageNumber(page);
                                    await handleGetTasks(page);
                                },
                            }}
                        />
                    </div>
                    <ConfigProvider
                        theme={{
                            token: {
                                colorPrimary: "#8b5cf6", // violet-500
                            }
                        }}
                    >
                        <Modal
                            width="50%"
                            title="Do you want to sign out?"
                            open={isSignOutModalOpen}
                            onOk={handleSignOutOk}
                            onCancel={handleSignOutCancel}
                        >
                        </Modal>
                        <Modal
                            width="50%"
                            title="Add new task"
                            open={isCreateTaskModalOpen}
                            onOk={async () => {
                                await handleCreateTaskOk();
                                await handleGetTasks(tasksListPageNumber);
                            }}
                            onCancel={handleCreateTaskCancel}
                        >
                            <div
                                className="flex flex-col gap-5 overflow-auto"
                            >
                                <Input
                                    value={newTaskDataTaskName}
                                    onChange={(e) => setNewTaskDataTaskName(e.target.value)}
                                    placeholder="Task title"/>
                                <Input
                                    value={newTaskDataTaskDetail}
                                    onChange={(e) => setNewTaskDataTaskDetail(e.target.value)}
                                    placeholder="Task content"/>
                                <DatePicker
                                    showTime
                                    onChange={(value, dateString) => {
                                        console.log('Selected Time: ', value);
                                        console.log('Formatted Selected Time: ', dateString);
                                        setNewTaskDataRemindAt(dateString + ".000")
                                    }}
                                    // onOk={onOk}
                                />
                            </div>
                        </Modal>
                    </ConfigProvider>
                </ConfigProvider>
                {
                    loading && <Loading/>
                }
            </div>
        </>
    );
}

export default Home;
