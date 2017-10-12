crypt();

function crypt() {
	const text = "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. " +
		"The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, " +
		"as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors " +
		"now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. " +
		"Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like)."
	const key = "00001101011111010111001110000110000011010111110101110011100001100000110101111101011100111000011000001101011111010111001110000110";
	const binaryText = text2Binary(text); //str
	let b = [];
	const keys = [];
	for (let i = 0; i < 4; i ++) {
		b.push(binaryText.slice(i * 32, (i + 1) * 32));
		keys.push(key.slice(i * 32, (i + 1) * 32));
	}
	console.log(b);
	b = cryptRound(b, keys);
	console.log(b);
	b = decryptRound(b, keys);
	console.log(b);
}

function cryptRound(b, keys) {
	b = b.map((text, index) => xor(text, keys[index]));
	const TO_LEFT = true;
	b[0] = cyclicShift(b[0], 13, TO_LEFT);
	b[2] = cyclicShift(b[2], 3, TO_LEFT);
	b[1] = xor(b[0], b[1], b[2]);
	b[3] = xor(b[3], b[2], shift(b[0], 3, TO_LEFT));
	b[1] = cyclicShift(b[1], 1, TO_LEFT);
	b[3] = cyclicShift(b[3], 7, TO_LEFT);
	b[0] = xor(b[0], b[1], b[3]);
	b[2] = xor(b[2], b[3], shift(b[1], 7, TO_LEFT));
	b[0] = cyclicShift(b[0], 5, TO_LEFT);
	b[2] = cyclicShift(b[2], 22, TO_LEFT);
	return b;
}

function decryptRound(b, keys) {
	const TO_RIGHT = false;
	b[2] = cyclicShift(b[2], 22, TO_RIGHT);
	b[0] = cyclicShift(b[0], 5, TO_RIGHT);
	b[2] = xor(b[2], b[3], shift(b[1], 7, TO_RIGHT));
	b[0] = xor(b[0], b[1], b[3]);
	b[3] = cyclicShift(b[3], 7, TO_RIGHT);
	b[1] = cyclicShift(b[1], 1, TO_RIGHT);
	b[3] = xor(b[3], b[2], shift(b[0], 3, TO_RIGHT));
	b[1] = xor(b[0], b[1], b[2]);
	b[2] = cyclicShift(b[2], 3, TO_RIGHT);
	b[0] = cyclicShift(b[0], 13, TO_RIGHT);
	b = b.map((text, index) => xor(text, keys[index]));
	return b;
}

function xor(...numbers) {
	let result = parseInt(numbers[0], 2);
	for (let i = 1; i < numbers.length; i ++) {
		const next = parseInt(numbers[i], 2);
		result = result ^ next;
	}
	return result.toString(2);
}

function shift(number, shift, toLeft) {
	if (toLeft) {
		return (parseInt(number, 2) << shift).toString(2);
	}
	return (parseInt(number, 2) >> shift).toString(2);
}

function cyclicShift(number, shift, toLeft) {
	const shiftedNumber = JSON.parse(JSON.stringify(number)).toString();
	const size = JSON.parse(JSON.stringify(number)).toString().length;
	for (let i = 0; i < size; i ++) {
		if (toLeft) {
			if ((i - shift) < 0) {
				shiftedNumber[i] = number[size - (i - shift)];
			} else {
				shiftedNumber[i] = number[i - shift];
			}
		} else {
			if ((i + shift) > (number.length - 1)) {
				shiftedNumber[i] = number[i + shift - number];
			} else {
				shiftedNumber[i] = number[i + shift];
			}
		}
	}
	return shiftedNumber;
}

function text2Binary(string) {
	return string.split(' ').map((char) => char.charCodeAt(0).toString(2)).join('');
}