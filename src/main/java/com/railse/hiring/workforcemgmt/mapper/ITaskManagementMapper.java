package com.railse.hiring.workforcemgmt.mapper;

import com.railse.hiring.workforcemgmt.dto.ActivityLogDto;
import com.railse.hiring.workforcemgmt.dto.CommentDto;
import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.model.ActivityLog;
import com.railse.hiring.workforcemgmt.model.Comment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ITaskManagementMapper {
   ITaskManagementMapper INSTANCE = Mappers.getMapper(ITaskManagementMapper.class);

   TaskManagementDto modelToDto(TaskManagement model);
   TaskManagement dtoToModel(TaskManagementDto dto);
   List<TaskManagementDto> modelListToDtoList(List<TaskManagement> models);
   
   ActivityLogDto activityLogToDto(ActivityLog activityLog);
   CommentDto commentToDto(Comment comment);
   
   List<ActivityLogDto> activityLogListToDtoList(List<ActivityLog> activityLogs);
   List<CommentDto> commentListToDtoList(List<Comment> comments);
}
