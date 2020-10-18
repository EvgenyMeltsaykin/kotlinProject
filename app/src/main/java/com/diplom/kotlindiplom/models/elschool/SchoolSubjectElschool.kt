package com.diplom.kotlindiplom.models.elschool

data class SchoolSubjectElschool(
    val ChangeName: String,
    val Number: Int,
    val StartTime: String,
    val EndTime: String,
    val RooId: Int,
    val InstituteId: Int,
    val DepartmentId: Int,
    val DepartmentName: String,
    val GroupId: Int?,
    val DisciplineId: Int,
    val DisciplineName: String,
    val PeriodId: Int
)