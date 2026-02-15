#!/usr/bin/env node

const { Document, Packer, Paragraph, Table, TableRow, TableCell, TextRun, HeadingLevel,
        BorderStyle, VerticalAlign, PageBreak, Header, Footer, Section, convertInchesToTwip } = require('docx');
const fs = require('fs');
const path = require('path');

// Define colors
const darkBlue = '1E3A5F';
const darkGray = '333333';
const lightGray = 'F0F0F0';

// Helper function to create heading
const createHeading = (text, level, color = darkBlue) => {
  const sizes = { 1: 56, 2: 44, 3: 24 };
  return new Paragraph({
    text: text,
    heading: level === 1 ? HeadingLevel.Heading1 : level === 2 ? HeadingLevel.Heading2 : HeadingLevel.Heading3,
    thematicBreak: false,
    style: `Heading${level}`,
    run: new TextRun({
      bold: true,
      size: sizes[level],
      color: color,
      font: 'Arial'
    })
  });
};

// Helper function to create title page
const createTitlePage = () => {
  return [
    new Paragraph({
      text: '',
      spacing: { line: 400 }
    }),
    new Paragraph({
      text: '',
      spacing: { line: 400 }
    }),
    new Paragraph({
      text: '',
      spacing: { line: 400 }
    }),
    new Paragraph({
      text: 'PLAYOUT EDGE',
      alignment: 'center',
      spacing: { line: 600 },
      run: new TextRun({
        bold: true,
        size: 72,
        color: darkBlue,
        font: 'Arial'
      })
    }),
    new Paragraph({
      text: '',
      spacing: { line: 200 }
    }),
    new Paragraph({
      text: 'Комплект материалов для отдела продаж',
      alignment: 'center',
      spacing: { line: 400 },
      run: new TextRun({
        size: 32,
        color: darkGray,
        font: 'Arial'
      })
    }),
    new Paragraph({
      text: '',
      spacing: { line: 600 }
    }),
    new Paragraph({
      text: '',
      spacing: { line: 600 }
    }),
    new Paragraph({
      text: '',
      spacing: { line: 600 }
    }),
    new Paragraph({
      text: '',
      spacing: { line: 600 }
    }),
    new Paragraph({
      text: 'Adapto | 2026',
      alignment: 'center',
      spacing: { line: 400 },
      run: new TextRun({
        size: 24,
        color: darkGray,
        font: 'Arial'
      })
    }),
    new Paragraph({
      text: '',
      spacing: { line: 300 }
    }),
    new Paragraph({
      text: 'КОНФИДЕНЦИАЛЬНО — Только для внутреннего использования',
      alignment: 'center',
      spacing: { line: 400 },
      run: new TextRun({
        bold: true,
        size: 24,
        color: 'CC0000',
        font: 'Arial'
      })
    }),
    new PageBreak()
  ];
};

// Section 1: О продукте
const section1 = [
  createHeading('О ПРОДУКТЕ', 1),
  new Paragraph({
    text: 'Что такое Playout Edge',
    spacing: { line: 300 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Playout Edge — это многоклиентская корпоративная платформа для автоматизации трансляций цифровых сигналов и управления контентом для телевизионных каналов, цифровых вывесок и систем очередей в реальном времени.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Платформа предоставляет полный набор инструментов для создания, планирования и управления видеоконтентом, медиа-активов и многоуровневых наложений в реальном времени, развертываемых на сотнях или тысячах устройств.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '',
    spacing: { line: 200 }
  }),
  new Paragraph({
    text: 'Для кого это предназначено',
    spacing: { line: 300 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Playout Edge ориентирован на:',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Телевизионные каналы, вещающие в Казахстане и странах СНГ',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Финансовые институты и банки (системы управления очередями, информационные панели)',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Сетевые розничные магазины и торговые центры',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Медицинские учреждения и клиники',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Государственные организации и органы власти',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '',
    spacing: { line: 300 }
  }),
  new Paragraph({
    text: 'Ключевые преимущества',
    spacing: { line: 300 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Доступная цена — в 3-5 раз дешевле мировых конкурентов (BrightSign, Scala)',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Быстрое развертывание — пилот за 4-6 недель, продакшн за 8-12 недель',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Локальная поддержка — техподдержка на казахском и русском языках, отклик в течение 2 часов',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Многоклиентская архитектура — один экземпляр для сотен клиентов, полная изоляция данных',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Гибкое развертывание — облако, on-prem, гибридное решение',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Открытые API — полная интеграция с существующими системами (ERP, CRM, медиа-серверы)',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '',
    spacing: { line: 300 }
  }),
  new Paragraph({
    text: 'Ключевые метрики производительности',
    spacing: { line: 300 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Table({
    width: { size: 100, type: 'pct' },
    rows: [
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Параметр', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center }),
          new TableCell({ children: [new Paragraph({ text: 'Значение', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Доступность платформы' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '99,9% uptime' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Время публикации расписания' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '≤ 2 минуты' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Задержка наложения данных' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '≤ 2 секунды' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Масштабируемость' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '10 000+ устройств' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Поддерживаемые форматы' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'MP4, HLS, WebP, PNG, JPEG, HTML5' })], shading: { fill: lightGray } })
        ]
      })
    ]
  }),
  new PageBreak()
];

// Section 2: Функциональные возможности
const section2 = [
  createHeading('ФУНКЦИОНАЛЬНЫЕ ВОЗМОЖНОСТИ', 1),
  new Paragraph({
    text: 'Playout Edge включает 13 ключевых модулей для полного управления контентом и устройствами:',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '',
    spacing: { line: 200 }
  }),
  new Table({
    width: { size: 100, type: 'pct' },
    rows: [
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Функция', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, width: { size: 30, type: 'pct' }, verticalAlign: VerticalAlign.center }),
          new TableCell({ children: [new Paragraph({ text: 'Описание', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, width: { size: 35, type: 'pct' }, verticalAlign: VerticalAlign.center }),
          new TableCell({ children: [new Paragraph({ text: 'Бизнес-преимущество', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, width: { size: 35, type: 'pct' }, verticalAlign: VerticalAlign.center })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '1. Управление каналами', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Создание и управление телевизионными каналами, переключение между ACTIVE/PAUSED/ARCHIVED' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Запуск нескольких каналов и проектов из единой платформы' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '2. Библиотека активов', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Загрузка видео, изображений, HTML, слайд-шоу. S3, локальное хранилище, SHA-256 хеширование, версионирование' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Централизованное управление контентом для всех устройств, защита от несанкционированных изменений' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '3. Управление расписанием', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Рабочий процесс Draft→Publish с контролем версий, диапазоны дат, дни недели, временные окна, взвешенная рандомизация' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Планирование контента за месяцы вперед, быстрые изменения с откатом в случае ошибки' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '4. Управление устройствами', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'QR-регистрация, мониторинг сердцебиения, статус онлайн/офлайн, удаленные действия (перезагрузка, перезапуск)' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Управление сотнями/тысячами устройств через веб-панель, мгновенное реагирование на сбои' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '5. Система наложений реального времени', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'SSE-базированные наложения данных (<2сек задержка). Виджеты: текст/тикер, таблицы очередей, KPI-плитки, QR-коды' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Динамический контент без перезапуска устройств, интеграция с живыми источниками данных' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '6. Аудит и соответствие', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Полный журнал всех действий администратора с дифами, экспорт CSV' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Соответствие нормативам, отслеживание ответственности, защита от ошибок' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '7. Проверка воспроизведения', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Документирование того, что фактически воспроизводилось на каком устройстве, с резюме воспроизведения' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Доказательство выполнения контрактов, оплата рекламы на основе фактических данных' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '8. Оповещения и мониторинг', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Устройство офлайн, актив недоступен, ошибка публикации, квота превышена' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Немедленная информация о проблемах, минимизация времени простоя' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '9. Контроль доступа (RBAC)', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Роли: Администратор, Оператор, Зритель, Интегратор. Тонкое управление разрешениями' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Безопасность, разделение обязанностей, аудит по ролям' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '10. Многоклиентность', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Полная изоляция данных, квоты на клиента, отдельные администраторы' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Один экземпляр серверного ПО для управления множеством независимых операторов' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '11. Android TV плеер', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Media3, Jetpack Compose, офлайн-кеш, логика откатного плана' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Надежное воспроизведение при потере интернета, современное UI/UX' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '12. MCP сервер', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '24+ JSON-RPC инструментов для программного доступа' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Интеграция с собственными системами, автоматизация рабочих процессов' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '13. Фоновые задачи', run: new TextRun({ bold: true, size: 22 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'PostgreSQL-базированный планировщик, без внешних очередей сообщений' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Упрощенное развертывание, минимум зависимостей' })], shading: { fill: lightGray } })
        ]
      })
    ]
  }),
  new PageBreak()
];

// Section 3: Конкурентный анализ
const section3 = [
  createHeading('КОНКУРЕНТНЫЙ АНАЛИЗ', 1),
  new Paragraph({
    text: 'Как Playout Edge сравнивается с конкурентами:',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '',
    spacing: { line: 200 }
  }),
  new Table({
    width: { size: 100, type: 'pct' },
    rows: [
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Критерий', run: new TextRun({ bold: true, color: 'FFFFFF', size: 20 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center }),
          new TableCell({ children: [new Paragraph({ text: 'Playout Edge', run: new TextRun({ bold: true, color: 'FFFFFF', size: 20 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center }),
          new TableCell({ children: [new Paragraph({ text: 'BrightSign', run: new TextRun({ bold: true, color: 'FFFFFF', size: 20 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center }),
          new TableCell({ children: [new Paragraph({ text: 'Scala', run: new TextRun({ bold: true, color: 'FFFFFF', size: 20 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center }),
          new TableCell({ children: [new Paragraph({ text: 'Navori', run: new TextRun({ bold: true, color: 'FFFFFF', size: 20 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Цена за устройство/месяц', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '4-20 USD' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '60-300 USD' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '100-500 USD' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '50-200 USD' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Скорость развертывания', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '4-6 недель' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '3-4 месяца' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '6-9 месяцев' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '8-12 недель' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Наложения реального времени', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Встроенные (<2сек)' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Ограниченные' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Плагины (долгие)' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Встроенные' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Аудит/соответствие', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Полный журнал' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Базовый' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Продвинутый' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Базовый' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Локальная поддержка', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Да (KZ/RU)' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Нет' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Нет' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Нет' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Многоклиентность' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Встроенная' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Нет' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Да' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Да' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Гибкость развертывания', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Cloud/On-prem' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Облако только' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'On-prem' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Облако' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Открытые API', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'REST + MCP' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'REST' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'REST + SDK' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'REST' })], shading: { fill: lightGray } })
        ]
      })
    ]
  }),
  new Paragraph({
    text: '',
    spacing: { line: 400 }
  }),
  new Paragraph({
    text: 'Выводы из конкурентного анализа:',
    spacing: { line: 300 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'BrightSign позиционируется как аппаратное решение, требует покупки дорогих плееров (300-600 USD каждый), отсутствует локальная поддержка в регионе',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Scala — традиционное решение высокого уровня с очень высокой ценой входа и длительным внедрением',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Navori — облачное SaaS с хорошей функциональностью, но без локальной поддержки',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Плayout Edge выигрывает по цене (в 3-5 раз дешевле), скорости внедрения и локальной поддержке',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new PageBreak()
];

// Section 4: Ценообразование
const section4 = [
  createHeading('ЦЕНООБРАЗОВАНИЕ И МОДЕЛИ ЛИЦЕНЗИРОВАНИЯ', 1),
  new Paragraph({
    text: 'Playout Edge предлагает гибкую трехуровневую модель ценообразования, адаптированную к размеру организации и масштабу развертывания:',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '',
    spacing: { line: 300 }
  }),
  createHeading('ПИЛОТ ПЛАН', 2),
  new Paragraph({
    text: 'До 50 устройств',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial',
      italics: true
    })
  }),
  new Paragraph({
    text: '$500/месяц',
    spacing: { line: 300 },
    run: new TextRun({
      bold: true,
      size: 28,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Включает:',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Управление каналами (до 5)',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Расписание и библиотека активов',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Базовые наложения (текст, тикер)',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Мониторинг устройств',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '30-дневный журнал аудита',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Идеально для: первых пилотных проектов, тестирования платформы перед расширением',
    spacing: { line: 400 },
    run: new TextRun({
      italics: true,
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('БИЗНЕС ПЛАН', 2),
  new Paragraph({
    text: 'До 500 устройств',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial',
      italics: true
    })
  }),
  new Paragraph({
    text: '$2,000/месяц',
    spacing: { line: 300 },
    run: new TextRun({
      bold: true,
      size: 28,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Все возможности Пилота, плюс:',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Неограниченные каналы',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Полные наложения (таблицы, KPI, QR)',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Интеграция источников данных',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Приоритетная поддержка (4 часа)',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Полный журнал аудита (1 год)',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Идеально для: средних и крупных коммерческих операций, многоканальных проектов',
    spacing: { line: 400 },
    run: new TextRun({
      italics: true,
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('ЭНТЕРПРАЙЗ ПЛАН', 2),
  new Paragraph({
    text: '500+ устройств',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial',
      italics: true
    })
  }),
  new Paragraph({
    text: 'По запросу',
    spacing: { line: 300 },
    run: new TextRun({
      bold: true,
      size: 28,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Все возможности Бизнес плана, плюс:',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'SSO/OIDC интеграция',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'SLA 99.9% доступность',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Выделенная техническая поддержка',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Пользовательские интеграции и разработка',
    spacing: { line: 150 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Выделенный сервер или облако',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Идеально для: национальных телекомпаний, государственных организаций, сетевого ритейла',
    spacing: { line: 400 },
    run: new TextRun({
      italics: true,
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('ЕДИНОВРЕМЕННЫЕ ЗАТРАТЫ', 2),
  new Paragraph({
    text: 'Настройка и развертывание: $2,000–$10,000 в зависимости от объема',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Интеграция с существующими системами: Дополнительно согласно договору',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Обучение команды: Включено в Бизнес и Энтерпрайз планы',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new PageBreak()
];

// Section 5: Целевые клиенты и сегменты
const section5 = [
  createHeading('ЦЕЛЕВЫЕ КЛИЕНТЫ И СЕГМЕНТЫ РЫНКА', 1),
  new Paragraph({
    text: 'Playout Edge позиционирует себя на нескольких ключевых сегментах казахстанского и СНГ рынков:',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '',
    spacing: { line: 200 }
  }),
  createHeading('1. ТЕЛЕВИЗИОННЫЕ КАНАЛЫ', 2),
  new Paragraph({
    text: 'Целевые клиенты: Местные, региональные и национальные телеканалы',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Проблема: Старые системы вещания требуют дорогого обслуживания, отсутствие интеграции с новыми инструментами',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Решение: Автоматизированное управление расписанием, наложения данных в реальном времени, полный контроль контента',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('2. ФИНАНСОВЫЕ УЧРЕЖДЕНИЯ И БАНКИ', 2),
  new Paragraph({
    text: 'Целевые клиенты: Крупные банки, микрофинансовые организации, обменные пункты',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Проблема: Управление очередями, отображение курсов валют и рекламы в реальном времени',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Решение: Виджеты для управления очередями, динамические наложения, интеграция с системами очередей',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('3. РОЗНИЧНЫЕ СЕТИ И ТОРГОВЫЕ ЦЕНТРЫ', 2),
  new Paragraph({
    text: 'Целевые клиенты: Супермаркеты, торговые центры, универсальные магазины',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Проблема: Множество экранов в разных точках, синхронизация контента, быстрое обновление рекламы',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Решение: Централизованное управление сотнями экранов, быстрые обновления контента, синхронизированное воспроизведение',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('4. ЗДРАВООХРАНЕНИЕ', 2),
  new Paragraph({
    text: 'Целевые клиенты: Государственные и частные клиники, больницы, медицинские центры',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Проблема: Управление очередями пациентов, информирование о времени приема, справочная информация',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Решение: Системы управления очередями, отображение номеров кабинетов, медицинская информация',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('5. ГОСУДАРСТВЕННЫЕ ОРГАНИЗАЦИИ', 2),
  new Paragraph({
    text: 'Целевые клиенты: Органы государственной власти, муниципальные учреждения, ГКЦ',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Проблема: Информирование граждан, информационные кампании, трансляция на офис-панели',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Решение: Контролируемое распространение информации, аудит всех действий, приватное развертывание',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('6. КОРПОРАТИВНЫЕ ОФИСЫ', 2),
  new Paragraph({
    text: 'Целевые клиенты: Корпоративные офисы крупных компаний, IT-компании, call-центры',
    spacing: { line: 200 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Проблема: Внутренние коммуникации, информация о результатах продаж, HR-информация',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Решение: KPI-панели, информация о компании в реальном времени, встроенные виджеты',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new PageBreak()
];

// Section 6: Скрипты продаж
const section6 = [
  createHeading('СКРИПТЫ ПРОДАЖ И РАЗГОВОРНЫЕ ТЕХНИКИ', 1),
  new Paragraph({
    text: 'Используйте следующие скрипты для различных этапов продажи:',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '',
    spacing: { line: 200 }
  }),
  createHeading('1. ХОЛОДНЫЙ ЗВОНОК (30 секунд)', 2),
  new Paragraph({
    text: '"Добрый день [ИМЯ], это [ВАШ ВОПРОС] из Adapto. Мы помогли более чем 50 телеканалам и финансовым организациям автоматизировать управление контентом и снизить затраты в 3-5 раз. У вас есть 30 секунд на быстрый разговор?"',
    spacing: { line: 300 },
    run: new TextRun({
      italics: true,
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Если "да": Перейти к Первой встречи',
    spacing: { line: 200 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Если "может быть": "Я отправлю вам краткое видео и буклет на почту. Когда подходит встреча на 20 минут?"',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('2. ПЕРВАЯ ВСТРЕЧА (5-10 минут)', 2),
  new Paragraph({
    text: 'Этап 1: Отложите питч на 2 минуты',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '"Спасибо за встречу. Прежде всего — у нас идет внедрение Playout Edge у [КОНКУРЕНТ/В СОСЕДНЕМ ГОРОДЕ]. Но я хочу понять вас лучше. Расскажите, как сейчас вы управляете контентом на экранах?"',
    spacing: { line: 300 },
    run: new TextRun({
      italics: true,
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Этап 2: Слушайте и выявляйте боли (2-3 минуты)',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Задавайте уточняющие вопросы:',
    spacing: { line: 200 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '"Сколько устройств сейчас у вас работает?"',
    spacing: { line: 100 },
    bullet: { level: 1 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '"Как часто вы обновляете контент?"',
    spacing: { line: 100 },
    bullet: { level: 1 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '"Какой бюджет вы тратите на управление (люди, ПО, железо)?"',
    spacing: { line: 100 },
    bullet: { level: 1 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '"Есть ли интеграции с другими системами (кассы, очереди, аналитика)?"',
    spacing: { line: 300 },
    bullet: { level: 1 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Этап 3: Питч решения (2-3 минуты)',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '"Понял. У нас есть решение, которое решает эти боли. Playout Edge позволяет автоматизировать ВСЕ управление контентом из единой панели. Вот что вы получите: [выберите самые релевантные]"',
    spacing: { line: 200 },
    run: new TextRun({
      italics: true,
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Снижение затрат на 40-60% за счет автоматизации',
    spacing: { line: 100 },
    bullet: { level: 1 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Обновление контента в реальном времени без перезапуска',
    spacing: { line: 100 },
    bullet: { level: 1 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Интеграция с вашими существующими системами через API',
    spacing: { line: 100 },
    bullet: { level: 1 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Локальная поддержка на казахском и русском языках',
    spacing: { line: 300 },
    bullet: { level: 1 },
    run: new TextRun({
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Этап 4: Демо и следующий шаг',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '"Я хочу вам показать 5-минутное видео. Или лучше я приду к вам на сайт, и мы посмотрим на вашу текущую установку и я скажу, как это работает у вас. Когда подходит среда?"',
    spacing: { line: 300 },
    run: new TextRun({
      italics: true,
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  }),
  createHeading('3. РАБОТА С ВОЗРАЖЕНИЯМИ', 2),
  new Paragraph({
    text: '',
    spacing: { line: 200 }
  }),
  new Table({
    width: { size: 100, type: 'pct' },
    rows: [
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Возражение', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center, width: { size: 25, type: 'pct' } }),
          new TableCell({ children: [new Paragraph({ text: 'Ваш ответ', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center, width: { size: 75, type: 'pct' } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '"Слишком дорого"' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Я это часто слышу. Но давайте посчитаем: если у вас 100 устройств и один оператор, это $200 в месяц в год = $2,400. Добавьте затраты на текущее обслуживание, обновления — обычно выходит $5,000-10,000 в год. Playout Edge: $2,000 месяц = $24,000 в год, но включает более 5 операторов и полную автоматизацию, которая избавляет вас от 1-2 человек.' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '"У нас свое решение"' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Спасибо, что делитесь. Что вас устраивает в вашем решении? Что не устраивает? (Обычно ответ: сложность, нужны специалисты, нет масштабирования). В этом и ценность Playout Edge — это готовое, масштабируемое решение, которое не требует хранения специалистов.' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '"Нужно подумать"' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Отлично, я это слышу. Обычно люди хотят увидеть это на практике. Давайте договоримся: я приду к вам на сайт на 2 часа, мы настроим пилот на 3 устройства, и вы сразу увидите, как это работает. Если не нравится — расходимся без обязательств. Когда?' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '"Безопасность, конфиденциальность"' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Отличный вопрос. Все данные хранятся на выделенном сервере в Казахстане, шифруются, и не передаются третьим лицам. У каждого клиента своя изолированная база данных. Мы предоставляем полный контроль доступа и аудит всех действий. Хотите видеть нашу документацию по безопасности?' })], shading: { fill: lightGray } })
        ]
      })
    ]
  }),
  new PageBreak()
];

// Section 7: FAQ
const section7 = [
  createHeading('ЧАСТО ЗАДАВАЕМЫЕ ВОПРОСЫ (FAQ)', 1),
  new Paragraph({
    text: '',
    spacing: { line: 200 }
  }),
  new Paragraph({
    text: 'В: Какое оборудование мне нужно?',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'О: Любой Android TV плеер (от дешевых xiaomi до профессиональных BrightSign). Можете перенести имеющееся оборудование.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'В: Сколько времени на внедрение?',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'О: Пилот (до 50 устройств): 4-6 недель. Продакшн (100-500): 8-12 недель. Зависит от сложности интеграций.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'В: Где хранятся мои данные?',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'О: На выбор: облако Казахстана, выделенный сервер, или on-prem (у вас в офисе). Все данные остаются вашими.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'В: Интеграция с нашей ERP?',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'О: Да, REST API и MCP (JSON-RPC). Стоимость интеграции: $2,000-5,000 в зависимости от сложности.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'В: Какая поддержка?',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'О: Telegram, email, телефон на казахском и русском. Пилот: 8 часов отклика. Бизнес/Энтерпрайз: 2-4 часа.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'В: Можно ли масштабировать с 50 на 500 устройств?',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'О: Да, просто обновите тариф. Нет никаких технических препятствий. Архитектура поддерживает 10,000+.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'В: Что если интернет упадет?',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'О: Устройства кешируют контент и продолжают воспроизводить его. Когда интернет вернется, синхронизируются автоматически.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'В: Какой ROI я получу?',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'О: Клиенты видят окупаемость за 6-12 месяцев за счет снижения затрат на персонал (1-2 человека), снижения простоев устройств и автоматизации контента.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'В: Какие гарантии?',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'О: 30-дневный пилот с полной гарантией возврата денег если не устроит. Энтерпрайз: SLA 99.9% uptime.',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new PageBreak()
];

// Section 8: Воронка продаж
const section8 = [
  createHeading('ВОРОНКА ПРОДАЖ И МЕТРИКИ', 1),
  new Paragraph({
    text: 'Типичная воронка продаж Playout Edge:',
    spacing: { line: 300 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: '',
    spacing: { line: 200 }
  }),
  new Table({
    width: { size: 100, type: 'pct' },
    rows: [
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: 'Стадия', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center, width: { size: 25, type: 'pct' } }),
          new TableCell({ children: [new Paragraph({ text: 'Описание', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center, width: { size: 25, type: 'pct' } }),
          new TableCell({ children: [new Paragraph({ text: 'Целевой процент', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center, width: { size: 25, type: 'pct' } }),
          new TableCell({ children: [new Paragraph({ text: 'Время', run: new TextRun({ bold: true, color: 'FFFFFF', size: 22 }) })], shading: { fill: darkBlue }, verticalAlign: VerticalAlign.center, width: { size: 25, type: 'pct' } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '1. ЛИД', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Холодный звонок, бизнес-карточка, рекомендация' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '—' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '—' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '↓ КВАЛИФИКАЦИЯ', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Проверка: есть ли бюджет, есть ли боль, есть ли авторитет решение' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '30%' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '1-2 недели' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '↓ ДЕМО', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Онлайн видео, на сайте, или пилот' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '60%' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '2-3 недели' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '↓ ПИЛОТ', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Развертывание на 5-20 устройства, проверка на месте' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '40%' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '4-8 недель' })], shading: { fill: lightGray } })
        ]
      }),
      new TableRow({
        children: [
          new TableCell({ children: [new Paragraph({ text: '↓ КОНТРАКТ', run: new TextRun({ bold: true, size: 20 }) })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: 'Подписание, оплата, масштабирование' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '70%' })], shading: { fill: lightGray } }),
          new TableCell({ children: [new Paragraph({ text: '1-2 недели' })], shading: { fill: lightGray } })
        ]
      })
    ]
  }),
  new Paragraph({
    text: '',
    spacing: { line: 300 }
  }),
  new Paragraph({
    text: 'Типичный цикл сделки: 2-4 месяца',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Средняя сумма контракта:',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Пилот: $500-1,000 + $2,000-5,000 развертывание = $2,500-6,000',
    spacing: { line: 100 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Год 1 (контракт): $24,000-240,000 в зависимости от плана и масштаба',
    spacing: { line: 100 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Год 2+: Повторяющиеся платежи, обычно на 10-20% выше',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'КПЭ для отслеживания:',
    spacing: { line: 200 },
    run: new TextRun({
      bold: true,
      size: 24,
      color: darkBlue,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Количество звонков в день: 10+',
    spacing: { line: 100 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Процент переводов на встречу: 20-30%',
    spacing: { line: 100 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Процент встреч → демо: 60-70%',
    spacing: { line: 100 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Процент демо → пилот: 40-50%',
    spacing: { line: 100 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Процент пилот → контракт: 70-80%',
    spacing: { line: 300 },
    bullet: { level: 0 },
    run: new TextRun({
      size: 24,
      color: darkGray,
      font: 'Arial'
    })
  }),
  new Paragraph({
    text: 'Пример: 100 лидов × 25% → 25 встреч × 65% → 16 демо × 45% → 7 пилотов × 75% → ~5 контрактов/месяц',
    spacing: { line: 300 },
    run: new TextRun({
      italics: true,
      size: 22,
      color: darkGray,
      font: 'Arial'
    })
  })
];

// Create sections array
const sections = [
  ...createTitlePage(),
  ...section1,
  ...section2,
  ...section3,
  ...section4,
  ...section5,
  ...section6,
  ...section7,
  ...section8
];

// Create document with header and footer
const doc = new Document({
  sections: [
    {
      properties: {
        page: {
          margins: {
            top: convertInchesToTwip(1),
            right: convertInchesToTwip(1),
            bottom: convertInchesToTwip(1),
            left: convertInchesToTwip(1)
          }
        }
      },
      headers: {
        default: new Header({
          children: [
            new Paragraph({
              text: 'Playout Edge — Sales Kit | КОНФИДЕНЦИАЛЬНО',
              spacing: { line: 240 },
              run: new TextRun({
                size: 20,
                color: darkGray,
                font: 'Arial'
              })
            })
          ]
        })
      },
      footers: {
        default: new Footer({
          children: [
            new Paragraph({
              alignment: 'center',
              text: '',
              spacing: { line: 240 },
              run: new TextRun({
                size: 20,
                color: darkGray,
                font: 'Arial'
              })
            })
          ]
        })
      },
      children: sections
    }
  ],
  styles: {
    default: {
      document: {
        run: {
          font: 'Arial',
          size: 24,
          color: darkGray
        }
      }
    }
  }
});

// Generate and save
Packer.toBuffer(doc).then(buffer => {
  const outputPath = '/sessions/focused-hopeful-ramanujan/mnt/adapto-board/Playout_Edge_Sales_Kit.docx';
  fs.writeFileSync(outputPath, buffer);
  console.log(`Document created successfully: ${outputPath}`);
  console.log(`File size: ${(buffer.length / 1024).toFixed(2)} KB`);
});
