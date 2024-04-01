export const dateToShortDateStr = (date) => {
    let day = date.getDate();
    let month = date.getMonth() + 1;
    const year = date.getFullYear();

    day = day < 10 ? '0' + day : day;
    month = month < 10 ? '0' + month : month;

    return `${day}.${month}.${year}`;
}

export const isoDateStrToShortDateStr = (dateStr) => {
    if (dateStr == null){
        return ""
    }
    const parts = dateStr.split('-');

    const convertedDate = `${parts[2]}.${parts[1]}.${parts[0]}`;

    return convertedDate;
}

export const isoDateTimeStrToShortDateStr = (dateStr) => {



    const dateObj = new Date(dateStr);

    if (dateStr == null){
        return ""
    }

    // Extract the day, month, and year from the Date object
    const day = String(dateObj.getDate()).padStart(2, '0');
    const month = String(dateObj.getMonth() + 1).padStart(2, '0'); // January is 0!
    const year = dateObj.getFullYear();

    // Format the date as dd.mm.yyyy
    const formattedDate = `${day}.${month}.${year}`;

    return formattedDate;
}

export const excelDateToDate = (excelDate) => {
    const utc_days = Math.floor(excelDate - 25569);
    const utc_value = utc_days * 86400;
    const date_info = new Date(utc_value * 1000);

    const fractional_day = excelDate - Math.floor(excelDate) + 0.0000001;

    let total_seconds = Math.floor(86400 * fractional_day);

    const seconds = total_seconds % 60;
    total_seconds -= seconds;

    const hours = Math.floor(total_seconds / (60 * 60));
    const minutes = Math.floor(total_seconds / 60) % 60;

    return new Date(date_info.getFullYear(), date_info.getMonth(), date_info.getDate(), hours, minutes, seconds);
}