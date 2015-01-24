package quentinc.sql;
import java.lang.annotation.*;
import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;

@Retention(RUNTIME)
@Target({FIELD,TYPE})
@interface SQLName {
String value () ;
}
