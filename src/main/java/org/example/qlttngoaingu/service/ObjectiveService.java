package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.ObjectiveRequest;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.Objective;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.CourseRepository;
import org.example.qlttngoaingu.repository.ObjectiveRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ObjectiveService {
    private final ObjectiveRepository objectiveRepository;
    private final CourseRepository courseRepository;

    public Objective addObjective(ObjectiveRequest objective,Integer courseId){
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        Objective objectiveToAdd = new Objective();
        objectiveToAdd.setObjectiveName(objective.getObjectiveName());
        course.getObjectives().add(objectiveToAdd);

        objectiveToAdd.setCourse(course);
        return objectiveRepository.save(objectiveToAdd);
    }
    public void updateObjective(Integer objectiveId,ObjectiveRequest objective){
        Objective objectiveToUpdate = objectiveRepository.getObjectiveById(objectiveId)
                .orElseThrow(() -> new AppException(ErrorCode.OBJECTIVE_NOT_FOUND));
        objectiveToUpdate.setObjectiveName(objective.getObjectiveName());
        objectiveRepository.save(objectiveToUpdate);
    }
    public void deleteObjective(Integer objectiveId){
        objectiveRepository.deleteById(objectiveId);
    }


}
