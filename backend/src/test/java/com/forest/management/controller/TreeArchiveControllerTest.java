package com.forest.management.controller;

import com.forest.management.model.TreeArchive;
import com.forest.management.service.DataGeneratorService;
import com.forest.management.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 林木档案控制器测试
 */
@WebMvcTest(TreeArchiveController.class)
public class TreeArchiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataGeneratorService dataGeneratorService;

    @MockBean
    private ValidationService validationService;

    @Autowired
    private ObjectMapper objectMapper;

    private TreeArchive testTree;
    private Map<Long, TreeArchive> testTreeMap;

    @BeforeEach
    void setUp() {
        testTree = new TreeArchive();
        testTree.setId(1L);
        testTree.setTreeSpecies("松树");
        testTree.setDiameter(25.5);
        testTree.setHeight(15.2);
        testTree.setHealthStatus("健康");
        testTree.setLocation("东山林区");
        testTree.setCreateTime(LocalDateTime.now());
        testTree.setUpdateTime(LocalDateTime.now());

        testTreeMap = new HashMap<>();
        testTreeMap.put(1L, testTree);
    }

    @Test
    void testGetTreeList() throws Exception {
        when(dataGeneratorService.getTreeArchives()).thenReturn(testTreeMap);

        mockMvc.perform(get("/api/trees")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void testGetTreeById() throws Exception {
        when(dataGeneratorService.getTreeArchives()).thenReturn(testTreeMap);

        mockMvc.perform(get("/api/trees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.treeSpecies").value("松树"))
                .andExpect(jsonPath("$.diameter").value(25.5))
                .andExpect(jsonPath("$.height").value(15.2))
                .andExpect(jsonPath("$.healthStatus").value("健康"));
    }

    @Test
    void testGetTreeByIdNotFound() throws Exception {
        when(dataGeneratorService.getTreeArchives()).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/trees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTree() throws Exception {
        TreeArchive newTree = new TreeArchive();
        newTree.setTreeSpecies("杉树");
        newTree.setDiameter(20.0);
        newTree.setHeight(12.0);
        newTree.setHealthStatus("良好");
        newTree.setLocation("西山林区");

        when(dataGeneratorService.getTreeArchives()).thenReturn(new HashMap<>());
        when(dataGeneratorService.generateTreeId()).thenReturn(2L);
        when(validationService.isValidTreeSpecies(any())).thenReturn(true);
        when(validationService.isValidDiameter(any())).thenReturn(true);
        when(validationService.isValidHeight(any())).thenReturn(true);
        when(validationService.isValidHealthStatus(any())).thenReturn(true);
        when(validationService.isValidLocation(any())).thenReturn(true);

        mockMvc.perform(post("/api/trees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTree)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("林木档案创建成功"))
                .andExpect(jsonPath("$.data.treeSpecies").value("杉树"));
    }

    @Test
    void testCreateTreeValidationError() throws Exception {
        TreeArchive invalidTree = new TreeArchive();
        invalidTree.setTreeSpecies(""); // 无效的树种名称
        invalidTree.setDiameter(200.0); // 无效的胸径
        invalidTree.setHeight(0.5); // 无效的树高
        invalidTree.setHealthStatus("无效状态");

        when(validationService.isValidTreeSpecies(any())).thenReturn(false);
        when(validationService.isValidDiameter(any())).thenReturn(false);
        when(validationService.isValidHeight(any())).thenReturn(false);
        when(validationService.isValidHealthStatus(any())).thenReturn(false);

        mockMvc.perform(post("/api/trees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTree)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("数据验证失败"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void testUpdateTree() throws Exception {
        TreeArchive updatedTree = new TreeArchive();
        updatedTree.setTreeSpecies("更新的松树");
        updatedTree.setDiameter(30.0);
        updatedTree.setHeight(18.0);
        updatedTree.setHealthStatus("良好");
        updatedTree.setLocation("更新的位置");

        when(dataGeneratorService.getTreeArchives()).thenReturn(testTreeMap);
        when(validationService.isValidTreeSpecies(any())).thenReturn(true);
        when(validationService.isValidDiameter(any())).thenReturn(true);
        when(validationService.isValidHeight(any())).thenReturn(true);
        when(validationService.isValidHealthStatus(any())).thenReturn(true);
        when(validationService.isValidLocation(any())).thenReturn(true);

        mockMvc.perform(put("/api/trees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTree)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("林木档案更新成功"))
                .andExpect(jsonPath("$.data.treeSpecies").value("更新的松树"));
    }

    @Test
    void testDeleteTree() throws Exception {
        when(dataGeneratorService.getTreeArchives()).thenReturn(testTreeMap);

        mockMvc.perform(delete("/api/trees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("林木档案删除成功"));
    }

    @Test
    void testDeleteTreeNotFound() throws Exception {
        when(dataGeneratorService.getTreeArchives()).thenReturn(new HashMap<>());

        mockMvc.perform(delete("/api/trees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("林木档案不存在"));
    }

    @Test
    void testGetStatistics() throws Exception {
        when(dataGeneratorService.getTreeArchives()).thenReturn(testTreeMap);

        mockMvc.perform(get("/api/trees/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.speciesStats").exists())
                .andExpect(jsonPath("$.healthStats").exists())
                .andExpect(jsonPath("$.avgDiameter").exists())
                .andExpect(jsonPath("$.avgHeight").exists());
    }
}