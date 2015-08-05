package functionaltests.utils;

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
