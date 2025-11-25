package org.example.qlttngoaingu.mapper;

import org.example.qlttngoaingu.dto.response.StudentInfo;
import org.example.qlttngoaingu.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentMapper {
    @Mappings({
            // Ánh xạ các trường từ Student Entity
            @Mapping(source = "id", target = "studentId"),
            @Mapping(source = "ngaySinh", target = "dateOfBirth"),
            @Mapping(source = "job", target = "jobs"),
            @Mapping(source = "avatar", target = "image"),
            @Mapping(source = "address", target = "address"),
            @Mapping(source = "gender",target = "gender"),

            // Ánh xạ các trường từ User Entity (thông qua trường 'account' trong Student)
            @Mapping(source = "account.email", target = "email"),
            @Mapping(source = "account.phoneNumber", target = "phoneNumber")
    })
    StudentInfo toStudentInfo(Student student);
}
