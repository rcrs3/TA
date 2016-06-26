package ta

import org.apache.ivy.core.settings.Validatable

import java.text.SimpleDateFormat
import java.lang.*

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class StudentController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    public static Date formattedDate(String dateInString) {
        def formatter = new SimpleDateFormat("dd/mm/yyyy");
        Date date = formatter.parse(dateInString);
        return date;
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Student.list(params), model: [studentInstanceCount: Student.count()]
    }


    public void checkConditionPercentage(String loginA, Report reportInstance) {
        double aux = checkPercentageEvaluationStudent(reportInstance.avaliacao, loginA)
        def controllerRepo = new ReportController()
        if (aux >= reportInstance.valor) {
            Student student = Student.findByLogin(loginA)
            controllerRepo.addStudentToReport(student, reportInstance)
        }
    }

    public void checkConditionAverage(Student student, Report reportInstance) {
        def controllerRepo = new ReportController()
        if (checkTotalAverage(student.average)) {
            controllerRepo.addStudentToReport(student, reportInstance)
        }
    }

    public double checkPercentageEvaluationStudent(String evalValue, String loginA) {
        def student = Student.findByLogin(loginA)
        int contE = 0
        def evaluationLists = student.criteriaAndEvaluations;
        int tamanho = 0;
        for (int i = 0; i < evaluationLists.size(); i++) {
            def evaluat = evaluationLists[i].getEvaluations()
            tamanho += evaluat.size()
            for (int j = 0; j < evaluat.size(); j++) {
                if (evaluat.get(i).value.equalsIgnoreCase(evalValue)) {
                    contE++
                }
            }
        }
        return contE / tamanho
    }

    def updateAllAverages() {
        Student.list().each {
            it.calcMedia()
        }
    }

    public boolean checkTotalAverage(double mediaAluno) {
        double media = 0
        for (Student student : Student.list()) {
            media += student.average
        }
        media = media / Student.list().size()
        if (mediaAluno >= media) {
            return true
        } else {
            return false
        }
    }

    public boolean addEvaluationsToAllStudents(LinkedList<Evaluation> evaluationList) {
        for(Report reports : Report.list()){
            reports.students = []
        }
        for (int i = 0; i < Student.list().size(); i++) {
            Student.list().get(i).addEvaluation(evaluationList.get(i))
            Student.list().get(i).save(
                    flush: true,
                    failOnError: true
            )
            for (Report report : Report.list()) {
                if (report.tipo.equalsIgnoreCase("Porcentagem")) {
                    checkConditionPercentage(Student.list().get(i).login, report)
                } else {
                    checkConditionAverage(Student.list().get(i), report)
                }
            }
        }
        return true
    }
    def addEvaToStudents(String criterionName, LinkedList<Evaluation> evaluations){

        Student.listOrderByLogin().each { Student student ->
            student.addEvaluation(evaluations.poll());
        }
    }
    //PARA TESTE- CUCUMBER
    public boolean addEvaluationToAllStudents() {
        def evaluationInstance = new Evaluation(params);
        Student.findAll().each { Student student ->
            student.addEvaluation(evaluationInstance);
            student.save flush: true
        }
        return true
    }
   //HAVIA CODIGO REPETIDO AQUI


//HAVIA CODIGO ULTRAPASSADO AQUI(CODIGO COM COMPORTAMENTO DA APLICACAO ANTIGA)

    public double checkPorcentageEvaluationStudent(String evalValue, String loginA) {
        def student = Student.findByLogin(loginA)
        def contE = 0
        def evaluationLists = student.criteriaAndEvaluations;
        int tamanho = 0;
        for (int i = 0; i < evaluationLists.size(); i++) {
            def evaluations = evaluationLists.get(i).evaluations;
            for (int j = 0; j < evaluations.size(); j++) {
                tamanho += evaluations.size();
                if (evaluations.get(i).value == evalValue) {
                    contE++;
                }
            }
        }
        return contE / tamanho;
    }


//  AQUI HAVIA CODIGO PARA TESTE, QUE NAO PRECISA MAIS

    def show(Student studentInstance) {
        respond studentInstance
    }

    def create() {
        respond new Student(params)
    }


    def search() {
        render view: "search"
    }

    def consult() {
        def auxList = Student.list()
        def studentList = auxList.findAll {
            it.name.toLowerCase().contains(params.consult.toLowerCase()) || it.login.toLowerCase().contains(params.consult.toLowerCase())
        }
        if (studentList == null) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'student.label', default: 'Student'), params.id])
            render view: "search", model: [studentInstanceList: [], studentInstanceCount: 0]
        } else {
            render view: "search", model: [studentInstanceList: studentList, studentInstanceCount: studentList.size()]
        }
    }

    @Transactional
    def save(Student studentInstance) {
        if (studentInstance == null) {
            notFound()
            return
        }
        if (studentInstance.hasErrors()) {
            respond studentInstance.errors, view: 'create'
            return
        }
        studentInstance.save flush: true
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'student.label', default: 'Student'), studentInstance.id])
                redirect studentInstance
            }
            '*' { respond studentInstance, [status: CREATED] }
        }
    }

    def edit(Student studentInstance) {
        respond studentInstance
    }

    @Transactional
    def update(Student studentInstance) {
        if (studentInstance == null) {
            notFound()
            return
        }
        if (studentInstance.hasErrors()) {
            respond studentInstance.errors, view: 'edit'
            return
        }

        studentInstance.save flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'student.label', default: 'Student'), studentInstance.id])
                redirect studentInstance
            }
            '*' { respond studentInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(Student studentInstance) {

        if (studentInstance == null) {
            notFound()
            return
        }

        studentInstance.delete flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'student.label', default: 'Student'), studentInstance.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'student.label', default: 'Student'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    public void groupSave(List<Student> group) {
        for (int i = 0; i < group.size(); i++) {
            group.get(i).save flush: true;
        }
    }

    def saveGroup() {
        String group = params.name
        String[] students = group.split(";")
        for (int i = 0; i < students.size(); i++) {
            List<String> token1 = students[i].tokenize(':')
            String info = token1.get(0)
            List<String> token2 = info.tokenize('(')
            String name = token2.get(0)
            String login = token2.get(1)
            Student novo = new Student(name, login)
            novo.calcMedia()

            if (Student.findByLogin(novo.getLogin()) == null) {
                novo.save flush: true
            }
        }

        flash.message = message(code: 'default.created.message', args: [message(code: students.length, 'student.label', default: 'Student')])

        redirect action: "index", method: "GET"
    }

    def createGroup() {
        respond view: 'createGroup'
    }
}




