package Production.AuthService.Constant;

import java.util.List;

public final class RoleConst {

    private RoleConst() {} // prevent instantiation

    public static final String ADMIN = "ADMIN";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String BACKSTAGE_USER = "BACKSTAGE_USER";

    public static final String FRONTSTAGE_USER = "FRONTSTAGE_USER";
    public static final String CLIENT = "CLIENT";
    public static final String POC = "POC";
    public static final String MANAGER = "MANAGER";
    public static final String STAKEHOLDER = "STAKEHOLDER";
    public static final String HR = "HR";
    public static final String DIRECTOR = "DIRECTOR";
    public static final String HOD = "HOD";
    public static final String DEVELOPER = "DEVELOPER";

    public static final String QA = "QA";
    public static final String DEFUALT = "GUEST";

    /** All roles in one place */
    public static final List<String> ALL_ROLES = List.of(
            ADMIN,
            CUSTOMER,
            BACKSTAGE_USER,
            FRONTSTAGE_USER,
            CLIENT,
            POC,
            MANAGER,
            STAKEHOLDER,
            HR,
            DIRECTOR,
            HOD,
            DEVELOPER,
            QA,
            DEFUALT
    );
}

