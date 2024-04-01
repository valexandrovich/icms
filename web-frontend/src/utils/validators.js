const countryCodes = [
    'AF',
    'AX',
    'AL',
    'DZ',
    'AS',
    'AD',
    'AO',
    'AI',
    'AQ',
    'AG',
    'AR',
    'AM',
    'AW',
    'AU',
    'AT',
    'AZ',
    'BS',
    'BH',
    'BD',
    'BB',
    'BY',
    'BE',
    'BZ',
    'BJ',
    'BM',
    'BT',
    'BO',
    'BQ',
    'BA',
    'BW',
    'BV',
    'BR',
    'IO',
    'BN',
    'BG',
    'BF',
    'BI',
    'CV',
    'KH',
    'CM',
    'CA',
    'KY',
    'CF',
    'TD',
    'CL',
    'CN',
    'CX',
    'CC',
    'CO',
    'KM',
    'CD',
    'CG',
    'CK',
    'CR',
    'CI',
    'HR',
    'CU',
    'CW',
    'CY',
    'CZ',
    'DK',
    'DJ',
    'DM',
    'DO',
    'EC',
    'EG',
    'SV',
    'GQ',
    'ER',
    'EE',
    'SZ',
    'ET',
    'FK',
    'FO',
    'FJ',
    'FI',
    'FR',
    'GF',
    'PF',
    'TF',
    'GA',
    'GM',
    'GE',
    'DE',
    'GH',
    'GI',
    'GR',
    'GL',
    'GD',
    'GP',
    'GU',
    'GT',
    'GG',
    'GN',
    'GW',
    'GY',
    'HT',
    'HM',
    'VA',
    'HN',
    'HK',
    'HU',
    'IS',
    'IN',
    'ID',
    'IR',
    'IQ',
    'IE',
    'IM',
    'IL',
    'IT',
    'JM',
    'JP',
    'JE',
    'JO',
    'KZ',
    'KE',
    'KI',
    'KP',
    'KR',
    'KW',
    'KG',
    'LA',
    'LV',
    'LB',
    'LS',
    'LR',
    'LY',
    'LI',
    'LT',
    'LU',
    'MO',
    'MG',
    'MW',
    'MY',
    'MV',
    'ML',
    'MT',
    'MH',
    'MQ',
    'MR',
    'MU',
    'YT',
    'MX',
    'FM',
    'MD',
    'MC',
    'MN',
    'ME',
    'MS',
    'MA',
    'MZ',
    'MM',
    'NA',
    'NR',
    'NP',
    'NL',
    'NC',
    'NZ',
    'NI',
    'NE',
    'NG',
    'NU',
    'NF',
    'MK',
    'MP',
    'NO',
    'OM',
    'PK',
    'PW',
    'PS',
    'PA',
    'PG',
    'PY',
    'PE',
    'PH',
    'PN',
    'PL',
    'PT',
    'PR',
    'QA',
    'RE',
    'RO',
    'RU',
    'RW',
    'BL',
    'SH',
    'KN',
    'LC',
    'MF',
    'PM',
    'VC',
    'WS',
    'SM',
    'ST',
    'SA',
    'SN',
    'RS',
    'SC',
    'SL',
    'SG',
    'SX',
    'SK',
    'SI',
    'SB',
    'SO',
    'ZA',
    'GS',
    'SS',
    'ES',
    'LK',
    'SD',
    'SR',
    'SJ',
    'SE',
    'CH',
    'SY',
    'TW',
    'TJ',
    'TZ',
    'TH',
    'TL',
    'TG',
    'TK',
    'TO',
    'TT',
    'TN',
    'TR',
    'TM',
    'TC',
    'TV',
    'UG',
    'UA',
    'AE',
    'GB',
    'UM',
    'US',
    'UY',
    'UZ',
    'VU',
    'VE',
    'VN',
    'VG',
    'VI',
    'WF',
    'EH',
    'YE',
    'ZM',
    'ZW'
]

const sexCodes = ['M', 'F']

export const isValidUaName = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    if (value.length > 64){
        return false;
    }
    return /^[А-ЯІЇЄҐ'-]+$/u.test(value) && !/[ЫЪ]/.test(value);
};

export const isValidRuName = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    if (value.length > 64){
        return false;
    }
    return /^[А-Я'-]+$/u.test(value) && !/[ІЇЄҐ]/.test(value);
};

export const isValidEnName = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    if (value.length > 64){
        return false;
    }
    return /^[a-zA-Z'-]+$/.test(value);
};

export const isValidDate = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true
    }
    if (value === '01.01.1900'){
        return false
    }
    const regex = /^(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[012])\.((19|20)\d\d)$/;
    return regex.test(value);
};

export const isValidLocalPassSerial = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return /^[А-ЯІЄЇ]{2}$/.test(value);
};

export const isValidIntPassSerial = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return /^[A-Z]{2}$/.test(value);
};

export const isValidLocalIntPassNumber = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return /^\d{6}$/.test(value);
};

export const isValidIdPassNumber = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return /^\d{9}$/.test(value);
};

export const isValidIdPassRecordNumber = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return /^\d{8}-\d{5}$/.test(value);
};

export const isValidIssuerCode = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return /^\d{4}$/.test(value);
};

export const isValidInn = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return /^\d{10}$/.test(value);
};

export const isValidPhone = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return /^\+?(\d[\s.-]?)?(\([\d]+\)|\d+)([\s.-]?\d{2,}){2,}$/g.test(value);
};

export const isValidVarchar255 = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return value.length <=255
}

export const isValidCountryCode = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return countryCodes.includes(value);
}

export const isValidSex = (value) => {
    if (value === null || value === '' || value === undefined) {
        return true;
    }
    return sexCodes.includes(value);
}