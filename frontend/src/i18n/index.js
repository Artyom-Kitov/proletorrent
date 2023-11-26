import ru from './ru.json'
import en from './en.json'

let cookieValue = document.cookie.replace(
    /(?:(?:^|.*;\s*)lang\s*\=\s*([^;]*).*$)|^.*$/,
    "$1",
);
export const defaultLocale = (cookieValue != null) ? cookieValue : 'ru'

export const languages = {
    en, ru
}