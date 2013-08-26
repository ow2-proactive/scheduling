package functionaltests;

public enum UserType {
    USER {
        public String toString() {
            return "user";
        }
    },
    ADMIN {
        public String toString() {
            return "admin";
        }
    }

}
