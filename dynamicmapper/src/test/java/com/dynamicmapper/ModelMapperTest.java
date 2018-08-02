package com.dynamicmapper;

import com.dynamicmapper.domain.Course;
import com.dynamicmapper.domain.Student;
import com.dynamicmapper.domain.StudentVO;
import com.dynamicmapper.domain.Subject;
import com.dynamicmapper.mapper.ModelMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.sound.sampled.Line;
import java.util.*;

import static com.dynamicmapper.mapper.ModelMapper.recursiveReflectiveDeepCopy;

public class ModelMapperTest {






    @Test
    public void testDeepCopy() {

        Student s1 = createStudent();
        LinkedList<Student> lst = new LinkedList();
        lst.add(s1);

        Course c1 = new Course("Computer Science", Arrays.asList( new Student[]{s1} ), lst);
        Course c2 = ModelMapper.deepCopyOf( c1 );
        Assert.assertTrue( c1.getTitle().equals(c2.getTitle()) );
    }




    @Test
    public void testSimpleObjectMapping(){

        Student s1 = createStudent();
        StudentVO vo = ModelMapper.map(s1, StudentVO.class);
        Assert.assertTrue( s1.getName().equals(vo.getFirstName()) );
    }

    @Test
    public void testListOfComplexObjectMapping(){

        Student s1 = createStudent();
        Student s2 = createStudent();
        s2.setName("Jane Doe");

        List<Student> studentList = Arrays.asList(s1, s2);
        List<StudentVO> studentListVO = null;
            studentListVO = ModelMapper.mapList(studentList, StudentVO.class);

        Assert.assertTrue(studentListVO!= null);
    }


    private Student createStudent() {
        Student s1 = new Student();
        s1.setEducationLevel("Bachelor");
        s1.setSubjects(new Subject[]{
                new Subject("ALG", "Algebra",   1),
                new Subject("PHYS1", "Physics", 1),
                new Subject("PG", "Programming",2),
        });
        s1.setAge(20);
        s1.setName("John Doe");
        return s1;
    }



}