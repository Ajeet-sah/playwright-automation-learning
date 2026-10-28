
const { test, expect } = require('@playwright/test');

test('home page', async ({ page }) => {

    await page.goto('https://menu.albertapayments.com/');

    const pageTitle = await page.title();

    console.log(pageTitle);

    await expect(page).toHaveTitle('kiosk-Alberta Payments');

});
