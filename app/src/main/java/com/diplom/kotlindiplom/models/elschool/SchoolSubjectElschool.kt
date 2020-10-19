package com.diplom.kotlindiplom.models.elschool

data class SchoolSubjectElschool(
    var ChangeName: String,
    var Number: Int,
    var StartTime: String,
    var EndTime: String,
    var RooId: Int,
    var InstituteId: Int,
    var DepartmentId: Int,
    var DepartmentName: String,
    var GroupId: Int?,
    var DisciplineId: Int,
    var DisciplineName: String,
    var PeriodId: Int
)