package com.taskmanager.controller;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createTask_ShouldReturnOk() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Task\", \"status\":\"TODO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllTasks_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void getTaskById_ShouldReturnTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        when(taskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(task);

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated\", \"status\":\"DONE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTask_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }
}
