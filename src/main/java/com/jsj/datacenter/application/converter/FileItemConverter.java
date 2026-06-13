package com.jsj.datacenter.application.converter;

import com.jsj.datacenter.adapter.dto.response.FileItemDTO;
import com.jsj.datacenter.infrastructure.fileitem.po.FileItemPO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/25
 */
@Mapper(componentModel = "spring")
public interface FileItemConverter {
    public FileItemDTO toDto(FileItemPO po);
    public List<FileItemDTO> toDtoList(List<FileItemPO> poList);
}
