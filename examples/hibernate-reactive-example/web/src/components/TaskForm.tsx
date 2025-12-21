import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import { taskService, Task } from "@/services/task.service";
import { toast } from "sonner";

const formSchema = z.object({
  taskTitle: z.string().min(1, "Title is required"),
  taskDetail: z.string().min(1, "Detail is required"),
  finished: z.boolean().optional(),
});

interface TaskFormProps {
  initialData?: Task;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export function TaskForm({ initialData, onSuccess, onCancel }: TaskFormProps) {
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      taskTitle: initialData?.taskTitle || "",
      taskDetail: initialData?.taskDetail || "",
      finished: initialData?.finished || false,
    },
  });

  async function onSubmit(values: z.infer<typeof formSchema>) {
    try {
      if (initialData) {
        const res = await taskService.updateTask({
          id: initialData.id,
          taskTitle: values.taskTitle,
          taskDetail: values.taskDetail,
          finished: values.finished || false,
        });
        if (res.errorCode === 100000) {
          toast.success("Task updated successfully");
          onSuccess?.();
        } else {
          toast.error(res.errorDescription);
        }
      } else {
        const res = await taskService.createTask({
          taskTitle: values.taskTitle,
          taskDetail: values.taskDetail,
        });
        if (res.errorCode === 100000) {
          toast.success("Task created successfully");
          onSuccess?.();
        } else {
          toast.error(res.errorDescription);
        }
      }
    } catch (error: any) {
      toast.error(error.message || "Something went wrong");
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="taskTitle"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Title</FormLabel>
              <FormControl>
                <Input placeholder="Task title" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="taskDetail"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Detail</FormLabel>
              <FormControl>
                <Textarea placeholder="Task detail" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        {initialData && (
          <FormField
            control={form.control}
            name="finished"
            render={({ field }) => (
              <FormItem className="flex flex-row items-start space-x-3 space-y-0 rounded-md border p-4">
                <FormControl>
                  <Checkbox
                    checked={field.value}
                    onCheckedChange={field.onChange}
                  />
                </FormControl>
                <div className="space-y-1 leading-none">
                  <FormLabel>Finished</FormLabel>
                </div>
              </FormItem>
            )}
          />
        )}
        <div className="flex justify-end gap-2">
          {onCancel && (
            <Button type="button" variant="outline" onClick={onCancel}>
              Cancel
            </Button>
          )}
          <Button type="submit">{initialData ? "Update" : "Create"}</Button>
        </div>
      </form>
    </Form>
  );
}
