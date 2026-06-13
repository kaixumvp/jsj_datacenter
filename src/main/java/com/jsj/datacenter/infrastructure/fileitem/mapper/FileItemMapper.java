package com.jsj.datacenter.infrastructure.fileitem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import com.jsj.datacenter.infrastructure.fileitem.po.FileItemPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/25
 */
@Mapper
public interface FileItemMapper extends BaseMapper<FileItemPO> {

    @Select(value = "select * from file_items where file_key=#{key} and deleted=0 limit 1")
    FileItemPO getFile(String key);

    @Select(value = "select * from file_items where file_type=#{fileType} and deleted=0 order by id desc")
    List<FileItemPO> queryFiles(UploadFileType fileType);

    @Update(value = "update file_items set deleted=1 where file_key=#{key}")
    void removeFile(String key);


    @Select("<script>select * from file_items where file_key IN " +
            "<foreach collection='keys' item='key' open='(' separator=',' close=')'>" +
            "#{key}" +
            "</foreach> order by create_time asc </script>")
    List<FileItemPO> queryFilesInKeys(@Param("keys")List<String> keys);
}
