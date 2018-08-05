package com.dynamicmapper;

import com.dynamicmapper.domain.*;
import com.dynamicmapper.mapper.ModelMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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


    @Test
    public void testCyclicReferenceOnMappingAndPass(){


        Person p1 = createPerson();
        p1.setParent(p1);
        PersonVO pvo = ModelMapper.map(p1,  PersonVO.class);
        Assert.assertTrue(pvo.getParent()==pvo);
    }



    @Test
    public void testMappingWithAgregationAndPass(){

        Person p1 = createPerson();
        p1.setParent(createPerson());
        PersonVO pvo = ModelMapper.map(p1, PersonVO.class);
        Assert.assertTrue(pvo.getParent()!=null);
    }




    private static Student createStudent() {
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

    private static Person createPerson(){
        return new Person("Jane Doe", 27);
    }



}