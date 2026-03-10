package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").build();
        task = Task.builder().id(1L).title("Test Task").user(user).build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createTask_ShouldReturnSavedTask() {
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setStatus("TODO");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task created = taskService.createTask(request);

        assertNotNull(created);
        assertEquals(task.getTitle(), created.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenOwner() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task found = taskService.getTaskById(1L);

        assertEquals(task.getId(), found.getId());
    }

    @Test
    void getTaskById_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    void getTaskById_ShouldThrowException_WhenNotOwner() {
        User otherUser = User.builder().id(2L).email("other@example.com").build();
        Task otherTask = Task.builder().id(2L).user(otherUser).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(otherTask));

        assertThrows(com.taskmanager.exception.UnauthorizedException.class, () -> taskService.getTaskById(2L));
    }

    @Test
    void getAuthenticatedUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> taskService.getAllTasks(org.springframework.data.domain.Pageable.unpaged()));
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Title");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task updated = taskService.updateTask(1L, request);

        assertNotNull(updated);
        assertEquals("Updated Title", updated.getTitle());
    }

    @Test
    void deleteTask_ShouldCallDelete() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).delete(task);
    }
}
