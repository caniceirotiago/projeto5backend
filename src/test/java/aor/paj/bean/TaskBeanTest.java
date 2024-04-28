package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TaskDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.exception.DatabaseOperationException;
import aor.paj.exception.EntityValidationException;
import aor.paj.exception.UserConfirmationException;
import aor.paj.service.websocket.TaskWebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaskBeanTest {

    @InjectMocks
    private TaskBean taskBean;

    @Mock
    private TaskDao taskDao;

    @Mock
    private UserDao userDao;

    @Mock
    private CategoryDao categoryDao;
    @Mock
    private UserBean userBean;
    @Mock
    private StatisticsBean statisticsBean;

    @Mock
    private TaskWebSocket taskWebSocket;

    private UserEntity testUser;
    private CategoryEntity testCategory;
    private TaskEntity testTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(taskDao, userDao, categoryDao, userBean, statisticsBean, taskWebSocket);

        testUser = new UserEntity();
        testUser.setUsername("testUser");
        testUser.setPassword("securePassword123");
        testUser.setEmail("testUser@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhoneNumber("123-456-7890");
        testUser.setPhotoURL("https://example.com/path");
        testUser.setToken("uniqueToken123456");
        testUser.setRole("USER");
        testUser.setDeleted(false);

        testCategory = new CategoryEntity();
        testCategory.setType("Work");
        testCategory.setAuthor(testUser);

        testTask = new TaskEntity();
        testTask.setTitle("Test Task");
        testTask.setDescription("This is a test task description.");
        testTask.setCategory(testCategory);
        testTask.setUser(testUser);
        testTask.setDeleted(false);



        when(userDao.findUserByUsername(anyString())).thenReturn(testUser);
        when(categoryDao.findCategoryByType(anyString())).thenReturn(testCategory);
        when(taskDao.findTaskById(anyInt())).thenReturn(testTask);
    }

    @Test
    void testAddTask_Success() throws Exception {
        TaskDto newTaskDto = new TaskDto();
        newTaskDto.setTitle("New Task");
        newTaskDto.setDescription("Description of the new task.");
        newTaskDto.setCategory_type("Work");
        newTaskDto.setUsername_author("testUser");
        newTaskDto.setStatus(100);
        newTaskDto.setPriority(1);

        when(userBean.getUserByToken("uniqueToken123456")).thenReturn(testUser);
        when(categoryDao.findCategoryByType("Work")).thenReturn(testCategory);

        taskBean.addTask("uniqueToken123456", newTaskDto);

        verify(taskDao).persist(any(TaskEntity.class));
        verify(statisticsBean, times(1)).broadcastTaskStatisticsUpdate();
    }

    @Test
    void testAddTask_Failure_InvalidToken() throws DatabaseOperationException {
        TaskDto newTaskDto = new TaskDto();
        newTaskDto.setTitle("New Task");
        newTaskDto.setDescription("Description of the new task.");
        newTaskDto.setCategory_type("Work");
        newTaskDto.setUsername_author("testUser");
        newTaskDto.setStatus(100);
        newTaskDto.setPriority(1);

        String invalidToken = "invalidToken123";

        when(userBean.getUserByToken(invalidToken)).thenReturn(null);
        assertThrows(UserConfirmationException.class, () -> {
            taskBean.addTask(invalidToken, newTaskDto);
        }, "Should throw UserConfirmationException for invalid token");

        verify(taskDao, never()).persist(any(TaskEntity.class));
        verify(statisticsBean, never()).broadcastTaskStatisticsUpdate();
    }
    @Test
    void testEditTask_Success() throws Exception {
        TaskDto taskDtoToUpdate = new TaskDto();
        taskDtoToUpdate.setId(1); // ID da tarefa existente
        taskDtoToUpdate.setTitle("Updated Title");
        taskDtoToUpdate.setDescription("Updated Description");
        taskDtoToUpdate.setCategory_type("Personal");
        taskDtoToUpdate.setStatus(200);
        taskDtoToUpdate.setPriority(2);

        TaskEntity existingTask = new TaskEntity();
        existingTask.setId(1);
        existingTask.setTitle("Original Title");
        existingTask.setDescription("Original Description");
        existingTask.setCategory(testCategory);
        existingTask.setUser(testUser);
        existingTask.setStatus(100);
        existingTask.setPriority(1);

        when(taskDao.findTaskById(1)).thenReturn(existingTask);
        when(categoryDao.findCategoryByType("Personal")).thenReturn(new CategoryEntity());

        taskBean.editTask(1, taskDtoToUpdate);

        verify(taskDao).merge(argThat(task ->
                task.getId() == 1 &&
                        "Updated Title".equals(task.getTitle()) &&
                        "Updated Description".equals(task.getDescription()) &&
                        task.getStatus() == 200 &&
                        task.getPriority() == 2
        ));

        verify(statisticsBean, times(1)).broadcastTaskStatisticsUpdate();
        if (existingTask.getStatus() != taskDtoToUpdate.getStatus()) {
            verify(taskWebSocket, times(1)).broadcast(eq("recycleTask"), any(TaskDto.class));
        }
    }
    @Test
    void testEditTask_TaskNotFound() {
        TaskDto taskDtoToUpdate = new TaskDto();
        taskDtoToUpdate.setTitle("Updated Title");
        taskDtoToUpdate.setDescription("Updated Description");
        taskDtoToUpdate.setCategory_type("Personal");
        taskDtoToUpdate.setStatus(200);
        taskDtoToUpdate.setPriority(2);

        when(taskDao.findTaskById(1)).thenReturn(null);

        assertThrows(EntityValidationException.class, () -> {
            taskBean.editTask(1, taskDtoToUpdate);
        });

        // Verificações adicionais para confirmar que nenhum método foi chamado devido à exceção
        verify(taskDao, never()).merge(any(TaskEntity.class)); // Nenhuma tarefa deve ser mesclada
        verify(statisticsBean, never()).broadcastTaskStatisticsUpdate(); // Nenhuma atualização de estatísticas deve ser disparada
    }

    @Test
    void testEditTask_CategoryNotFound() throws UnknownHostException {
        TaskDto taskDtoToUpdate = new TaskDto();
        taskDtoToUpdate.setTitle("Updated Title");
        taskDtoToUpdate.setDescription("Updated Description");
        taskDtoToUpdate.setCategory_type("Nonexistent Category");
        taskDtoToUpdate.setStatus(200);
        taskDtoToUpdate.setPriority(2);

        TaskEntity existingTask = new TaskEntity();
        existingTask.setId(1);
        existingTask.setTitle("Original Title");
        existingTask.setDescription("Original Description");
        existingTask.setCategory(testCategory);
        existingTask.setUser(testUser);
        existingTask.setStatus(100);
        existingTask.setPriority(1);

        when(taskDao.findTaskById(1)).thenReturn(existingTask);
        when(categoryDao.findCategoryByType("Nonexistent Category")).thenReturn(null);

        assertThrows(EntityValidationException.class, () -> {
            taskBean.editTask(1, taskDtoToUpdate);
        });

        verify(taskDao, never()).merge(any(TaskEntity.class));
        verify(statisticsBean, never()).broadcastTaskStatisticsUpdate();
    }


    @Test
    void testGetTaskById_Found() {
        TaskEntity result = taskBean.getTaskById(1);
        assertNotNull(result, "Task should be found");
        assertEquals(testTask.getTitle(), result.getTitle(), "The retrieved task should have the correct title");
    }
    @Test
    void deleteTaskPermanently_Success() throws Exception {

        when(taskDao.findTaskById(1)).thenReturn(testTask);
        taskBean.deleteTaskPermanently(1);
        verify(taskDao).deleteTask(1);
    }

    @Test
    void deleteTaskPermanently_Failure_TaskNotFound() {
        when(taskDao.findTaskById(anyInt())).thenReturn(null);

        assertThrows(EntityValidationException.class, () -> {
            taskBean.deleteTaskPermanently(999); // ID inexistente
        });

        verify(taskDao, never()).deleteTask(anyInt());
    }



}
