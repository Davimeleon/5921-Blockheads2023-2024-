package org.firstinspires.ftc.teamcode.driveModes;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.checkerframework.checker.signedness.qual.Constant;
import org.firstinspires.ftc.teamcode.common.Button;
import org.firstinspires.ftc.teamcode.common.Constants;
import org.firstinspires.ftc.teamcode.common.HardwareDrive;
import org.firstinspires.ftc.teamcode.common.Utility;

import java.security.KeyStore;

@TeleOp(name = "Base Drive Tests", group = "Drive")
//@Disabled
public class BaseDriveTests extends LinearOpMode {
    /* Declare OpMode members. */
    HardwareDrive robot = new HardwareDrive();
    private Constants constants = new Constants();
    private CRServo serv0;
    private ElapsedTime runtime = new ElapsedTime();
    private Button lifterButton = new Button();
    private Button lifterBottomButton = new Button();
    private boolean toggleButton = true;
    int lTgtPos = 0;


    @Override
    public void runOpMode() {
        serv0 = hardwareMap.get(CRServo.class, "serv0");
        robot.init(hardwareMap);
        telemetry.addData("Say", "Hello Driver");
        runtime.reset();
        robot.lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        waitForStart();
        while (opModeIsActive()) loop1();
    }
    private void loop1() {

        if (gamepad2.y) {
            lTgtPos = Constants.elevatorPositionTop;
        } else if (gamepad2.x) {
            lTgtPos = Constants.elevatorPositionMid;
        } else if (gamepad2.a) {
            lTgtPos = Constants.elevatorPositionLow;
        } else if (gamepad2.b) {
            lTgtPos = Constants.elevatorPositionBottom - 200;
        } else if (gamepad2.dpad_down || gamepad2.dpad_left || gamepad2.dpad_right || gamepad2.dpad_up) {
            lTgtPos = Constants.elevatorPositionBottom;
        }

        telemetry.addData("Difference between target and current positions", lTgtPos - robot.lift.getCurrentPosition());
        sleep(1000);

        if (lTgtPos - robot.lift.getCurrentPosition() > 100) { // lift is too high
            robot.lift.setPower(0.2);
        } else if (lTgtPos - robot.lift.getCurrentPosition() < 100) { // lift is too low
            robot.lift.setPower(-0.2);
        } else {
            robot.lift.setPower(-0.01);
        }

        double drivePower = 0.25;
        if (gamepad1.right_bumper) drivePower = 1;
        else if (gamepad1.left_bumper) drivePower = 0.25;
        DriveTrainBase(drivePower, lTgtPos);
        DriveMicroAdjust(0.4);

        UpdateGripper();
        UpdateTelemetry();
    }

    @Utility.Encapsulate
    private void DriveTrainBase(double drivePower, int lTgtPos) {
        double directionX = Math.pow(gamepad1.left_stick_x, 1); // Strafe
        double directionY = Math.pow(gamepad1.left_stick_y, 1); // Forward
        double directionR = -Math.pow(gamepad1.right_stick_x, 1); // Turn
        // double liftPower = Math.pow(gamepad2.right_stick_y, 1); // Lift

        //dead zones
        if (gamepad1.left_stick_x < 0.2 && gamepad1.left_stick_x > -0.2) {directionX = 0;}
        if (gamepad1.left_stick_y < 0.2 && gamepad1.left_stick_y > -0.2) {directionY = 0;}

        robot.lf.setPower((directionY + directionR - directionX) * drivePower);
        robot.rf.setPower((-directionY + directionR - directionX) * drivePower);
        robot.lb.setPower((directionY + directionR + directionX) * drivePower);
        robot.rb.setPower((-directionY + directionR + directionX) * drivePower);

    }
    private void DriveMicroAdjust(double power) {
        if (gamepad1.dpad_up) {
            robot.lf.setPower(-power);
            robot.rf.setPower(+power);
            robot.lb.setPower(-power);
            robot.rb.setPower(+power);
        } else if (gamepad1.dpad_down) {
            robot.lf.setPower(+power);
            robot.rf.setPower(-power);
            robot.lb.setPower(+power);
            robot.rb.setPower(-power);
        } else if (gamepad1.dpad_right) {
            robot.lf.setPower(power);
            robot.rf.setPower(power);
            robot.lb.setPower(power);
            robot.rb.setPower(power);
        } else if (gamepad1.dpad_left) {
            robot.lf.setPower(-power);
            robot.rf.setPower(-power);
            robot.lb.setPower(-power);
            robot.rb.setPower(-power);
        }

        if (gamepad1.left_trigger == 1) {
            robot.lf.setPower(-power);
            robot.rf.setPower(power);
            robot.lb.setPower(-power);
            robot.rb.setPower(power);
        } else if (gamepad1.right_trigger == 1) {
            robot.lf.setPower(power);
            robot.rf.setPower(-power);
            robot.lb.setPower(power);
            robot.rb.setPower(-power);
        }
    }
    private void MoveLiftTo(int jLevel) {
        telemetry.addData("Beginning MoveLiftTo; jLevel ", jLevel);
        telemetry.update();
        int jCounts = Constants.elevatorPositionBottom;
        switch (jLevel) {
            case -1:
                jCounts = Constants.elevatorPositionBottom;
            case 0:
                jCounts = Constants.elevatorPositionBottom - 100; //ground junction
            case 1:
                jCounts = Constants.elevatorPositionLow;
            case 2:
                jCounts = Constants.elevatorPositionMid;
            case 3:
                jCounts = Constants.elevatorPositionTop;
        }
        robot.lift.setTargetPosition(jCounts);
        robot.lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.lift.setPower(0.7);
        telemetry.addData("Lift being given power and sent to ", jLevel);
        telemetry.update();
        sleep(jCounts * 750);
        if (jLevel != -1) {
            robot.lift.setTargetPosition(jCounts + 5);
            robot.lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.lift.setPower(0.01);
        }
    }
    private void UpdateGripper() {
        if (gamepad2.left_trigger > 0.01) serv0.setPower(0.22 * gamepad2.left_trigger - 0);
        else if  (gamepad2.right_trigger > 0.01) serv0.setPower(-0.1 * gamepad2.right_trigger + 0);
    }
    private void UpdateTelemetry() {
        telemetry.addData("g1.X", gamepad1.left_stick_x);
        telemetry.addData("g1.Y", -gamepad1.left_stick_y);
        telemetry.addData("g1.R", gamepad1.right_stick_x);
        telemetry.addData("Arm Position", robot.lift.getCurrentPosition());
        telemetry.addData("g2.L", gamepad2.right_stick_y);
        telemetry.update();
    }
}
